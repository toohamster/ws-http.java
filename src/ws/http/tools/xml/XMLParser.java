package ws.http.tools.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class XMLParser {

    /** The Character '&amp;'. */
    public static final Character AMP = '&';

    /** The Character '''. */
    public static final Character APOS = '\'';

    /** The Character '!'. */
    public static final Character BANG = '!';

    /** The Character '='. */
    public static final Character EQ = '=';

    /** The Character '>'. */
    public static final Character GT = '>';

    /** The Character '&lt;'. */
    public static final Character LT = '<';

    /** The Character '?'. */
    public static final Character QUEST = '?';

    /** The Character '"'. */
    public static final Character QUOT = '"';

    /** The Character '/'. */
    public static final Character SLASH = '/';
    
    @SuppressWarnings("unchecked")
	private static void accumulate(HashMap<String, Object> context, String key, Object value) throws XMLException {
        Object object = context.get(key);
        
        if (object == null) {
        	context.put(key,value);
        } else if (object instanceof List) {
        	ArrayList<Object> arrayList = ((ArrayList<Object>) object);
			arrayList.add(value);
        } else {
        	// key 存在,变更成数组类型
        	ArrayList<Object> list = new ArrayList<Object>();
        	list.add(object);
        	list.add(value);
        	context.put(key, list);
        }
    }

    /**
     * Scan the content following the named tag, attaching it to the context.
     * 
     * @param x
     *            The XMLTokener containing the source string.
     * @param context
     *            The HashMap that will include the new material.
     * @param name
     *            The tag name.
     * @return true if the close tag is processed.
     * @throws XMLException
     */
    private static boolean parse(XMLTokener x, HashMap<String, Object> context, String name, boolean keepStrings)
            throws XMLException {
        char c;
        int i;
        HashMap<String, Object> HashMap = null;
        String string;
        String tagName;
        Object token;

        // Test for and skip past these forms:
        // <!-- ... -->
        // <! ... >
        // <![ ... ]]>
        // <? ... ?>
        // Report errors for these forms:
        // <>
        // <=
        // <<

        token = x.nextToken();

        // <!

        if (token == BANG) {
            c = x.next();
            if (c == '-') {
                if (x.next() == '-') {
                    x.skipPast("-->");
                    return false;
                }
                x.back();
            } else if (c == '[') {
                token = x.nextToken();
                if ("CDATA".equals(token)) {
                    if (x.next() == '[') {
                        string = x.nextCDATA();
                        if (string.length() > 0) {
                        	accumulate(context, "content", string);
                        }
                        return false;
                    }
                }
                throw x.syntaxError("Expected 'CDATA['");
            }
            i = 1;
            do {
                token = x.nextMeta();
                if (token == null) {
                    throw x.syntaxError("Missing '>' after '<!'.");
                } else if (token == LT) {
                    i += 1;
                } else if (token == GT) {
                    i -= 1;
                }
            } while (i > 0);
            return false;
        } else if (token == QUEST) {

            // <?
            x.skipPast("?>");
            return false;
        } else if (token == SLASH) {

            // Close tag </

            token = x.nextToken();
            if (name == null) {
                throw x.syntaxError("Mismatched close tag " + token);
            }
            if (!token.equals(name)) {
                throw x.syntaxError("Mismatched " + name + " and " + token);
            }
            if (x.nextToken() != GT) {
                throw x.syntaxError("Misshaped close tag");
            }
            return true;

        } else if (token instanceof Character) {
            throw x.syntaxError("Misshaped tag");

            // Open tag <

        } else {
            tagName = (String) token;
            token = null;
            HashMap = new HashMap<String, Object>();
            for (;;) {
                if (token == null) {
                    token = x.nextToken();
                }

                // attribute = value
                if (token instanceof String) {
                    string = (String) token;
                    token = x.nextToken();
                    if (token == EQ) {
                        token = x.nextToken();
                        if (!(token instanceof String)) {
                            throw x.syntaxError("Missing value");
                        }
                        
                        accumulate(HashMap, string, keepStrings ? token : (String) token );
                        token = null;
                    } else {
                    	accumulate(HashMap, string, "");
                    }


                } else if (token == SLASH) {
                    // Empty tag <.../>
                    if (x.nextToken() != GT) {
                        throw x.syntaxError("Misshaped tag");
                    }
                    if (HashMap.size() > 0) {
                    	accumulate(context, tagName, HashMap);
                    } else {
                    	accumulate(context, tagName, "");
                    }
                    return false;

                } else if (token == GT) {
                    // Content, between <...> and </...>
                    for (;;) {
                        token = x.nextContent();
                        if (token == null) {
                            if (tagName != null) {
                                throw x.syntaxError("Unclosed tag " + tagName);
                            }
                            return false;
                        } else if (token instanceof String) {
                            string = (String) token;
                            if (string.length() > 0) {
                            	accumulate(HashMap, "content", keepStrings ? token : (String) token );
                                
                            }

                        } else if (token == LT) {
                            // Nested element
                            if (parse(x, HashMap, tagName,keepStrings)) {
                                if (HashMap.size() == 0) {
                                	accumulate(context, tagName, "");
                                } else if (HashMap.size() == 1
                                        && HashMap.get("content") != null) {
                                	
                                	accumulate(context, tagName, HashMap.get("content"));
                                } else {
                                    accumulate(context, tagName, HashMap);
                                }
                                return false;
                            }
                        }
                    }
                } else {
                    throw x.syntaxError("Misshaped tag");
                }
            }
        }
    }
    
    public static HashMap<String, Object> toMap(String string) throws XMLException {
        return toMap(string, false);
    }

    /**
     * Convert a well-formed (but not necessarily valid) XML string into a
     * HashMap. Some information may be lost in this transformation because
     * JSON is a data format and XML is a document format. XML uses elements,
     * attributes, and content text, while JSON uses unordered collections of
     * name/value pairs and arrays of values. JSON does not does not like to
     * distinguish between elements and attributes. Sequences of similar
     * elements are represented as JSONArrays. Content text may be placed in a
     * "content" member. Comments, prologs, DTDs, and <code>&lt;[ [ ]]></code>
     * are ignored.
     * 
     * All values are converted as strings, for 1, 01, 29.0 will not be coerced to
     * numbers but will instead be the exact value as seen in the XML document.
     * 
     * @param string
     *            The source string.
     * @param keepStrings If true, then values will not be coerced into boolean
     *  or numeric values and will instead be left as strings
     * @return A HashMap containing the structured data from the XML string.
     * @throws XMLException Thrown if there is an errors while parsing the string
     */
    public static HashMap<String, Object> toMap(String string, boolean keepStrings) throws XMLException {
    	HashMap<String, Object> map = new HashMap<String, Object>();
        XMLTokener x = new XMLTokener(string);
        while (x.more() && x.skipPast("<")) {
            parse(x, map, null, keepStrings);
        }
        return map;
    }

}
