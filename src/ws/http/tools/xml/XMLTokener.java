package ws.http.tools.xml;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

public class XMLTokener {

	private boolean eof;
	private long index;
	private char previous;
	private Reader reader;
	private boolean usePrevious;

	/**
	 * The table of entity values. It initially contains Character values for
	 * amp, apos, gt, lt, quot.
	 */
	public static final java.util.HashMap<String, Character> entity;

	static {
		entity = new java.util.HashMap<String, Character>(8);
		entity.put("amp", XMLParser.AMP);
		entity.put("apos", XMLParser.APOS);
		entity.put("gt", XMLParser.GT);
		entity.put("lt", XMLParser.LT);
		entity.put("quot", XMLParser.QUOT);
	}

	/**
	 * Construct an XMLTokener from a string.
	 * 
	 * @param s
	 *            A source string.
	 */
	public XMLTokener(String s) {
		this(new StringReader(s));
	}

	/**
	 * Construct a XMLTokener from a Reader.
	 *
	 * @param reader
	 *            A reader.
	 */
	public XMLTokener(Reader reader) {
		this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
		this.eof = false;
		this.usePrevious = false;
		this.previous = 0;
		this.index = 0;
	}

	/**
	 * Construct a XMLTokener from an InputStream.
	 * 
	 * @param inputStream
	 *            The source.
	 */
	public XMLTokener(InputStream inputStream) {
		this(new InputStreamReader(inputStream));
	}

	/**
	 * Make a XMLException to signal a syntax error.
	 *
	 * @param message
	 *            The error message.
	 * @return A XMLException object, suitable for throwing
	 */
	public XMLException syntaxError(String message) {
		return new XMLException(message + this.toString());
	}

	/**
	 * @return true if at the end of the file and we didn't step back
	 */
	public boolean end() {
		return this.eof && !this.usePrevious;
	}

	/**
	 * Get the next character in the source string.
	 *
	 * @return The next character, or 0 if past the end of the source string.
	 * @throws XMLException
	 *             Thrown if there is an error reading the source string.
	 */
	public char next() throws XMLException {
		int c;
		if (this.usePrevious) {
			this.usePrevious = false;
			c = this.previous;
		} else {
			try {
				c = this.reader.read();
			} catch (IOException exception) {
				throw new XMLException(exception);
			}

			if (c <= 0) { // End of stream
				this.eof = true;
				c = 0;
			}
		}
		this.index += 1;
//		if (this.previous == '\r') {
//		} else if (c == '\n') {
//		} else {
//		}
		this.previous = (char) c;
		return this.previous;
	}

	/**
	 * Get the text in the CDATA block.
	 * 
	 * @return The string up to the <code>]]&gt;</code>.
	 * @throws XMLException
	 *             If the <code>]]&gt;</code> is not found.
	 */
	public String nextCDATA() throws XMLException {
		char c;
		int i;
		StringBuilder sb = new StringBuilder();
		for (;;) {
			c = next();
			if (end()) {
				throw syntaxError("Unclosed CDATA");
			}
			sb.append(c);
			i = sb.length() - 3;
			if (i >= 0 && sb.charAt(i) == ']' && sb.charAt(i + 1) == ']' && sb.charAt(i + 2) == '>') {
				sb.setLength(i);
				return sb.toString();
			}
		}
	}

	/**
	 * Get the next XML outer token, trimming whitespace. There are two kinds of
	 * tokens: the '<' character which begins a markup tag, and the content text
	 * between markup tags.
	 *
	 * @return A string, or a '<' Character, or null if there is no more source
	 *         text.
	 * @throws XMLException
	 */
	public Object nextContent() throws XMLException {
		char c;
		StringBuilder sb;
		do {
			c = next();
		} while (Character.isWhitespace(c));
		if (c == 0) {
			return null;
		}
		if (c == '<') {
			return XMLParser.LT;
		}
		sb = new StringBuilder();
		for (;;) {
			if (c == '<' || c == 0) {
				back();
				return sb.toString().trim();
			}
			if (c == '&') {
				sb.append(nextEntity(c));
			} else {
				sb.append(c);
			}
			c = next();
		}
	}

	/**
	 * Determine if the source string still contains characters that next() can
	 * consume.
	 * 
	 * @return true if not yet at the end of the source.
	 * @throws XMLException
	 *             thrown if there is an error stepping forward or backward
	 *             while checking for more data.
	 */
	public boolean more() throws XMLException {
		this.next();
		if (this.end()) {
			return false;
		}
		this.back();
		return true;
	}

	/**
	 * Back up one character. This provides a sort of lookahead capability, so
	 * that you can test for a digit or letter before attempting to parse the
	 * next number or identifier.
	 * 
	 * @throws XMLException
	 *             Thrown if trying to step back more than 1 step or if already
	 *             at the start of the string
	 */
	public void back() throws XMLException {
		if (this.usePrevious || this.index <= 0) {
			throw new XMLException("Stepping back two steps is not supported");
		}
		this.index -= 1;
		this.usePrevious = true;
		this.eof = false;
	}

	/**
	 * Return the next entity. These entities are translated to Characters:
	 * <code>&amp;  &apos;  &gt;  &lt;  &quot;</code>.
	 * 
	 * @param ampersand
	 *            An ampersand character.
	 * @return A Character or an entity String if the entity is not recognized.
	 * @throws XMLException
	 *             If missing ';' in XML entity.
	 */
	public Object nextEntity(char ampersand) throws XMLException {
		StringBuilder sb = new StringBuilder();
		for (;;) {
			char c = next();
			if (Character.isLetterOrDigit(c) || c == '#') {
				sb.append(Character.toLowerCase(c));
			} else if (c == ';') {
				break;
			} else {
				throw syntaxError("Missing ';' in XML entity: &" + sb);
			}
		}
		String string = sb.toString();
		Object object = entity.get(string);
		return object != null ? object : ampersand + string + ";";
	}

	/**
	 * Returns the next XML meta token. This is used for skipping over <!...>
	 * and <?...?> structures.
	 * 
	 * @return Syntax characters (<code>< > / = ! ?</code>) are returned as
	 *         Character, and strings and names are returned as Boolean. We
	 *         don't care what the values actually are.
	 * @throws XMLException
	 *             If a string is not properly closed or if the XML is badly
	 *             structured.
	 */
	public Object nextMeta() throws XMLException {
		char c;
		char q;
		do {
			c = next();
		} while (Character.isWhitespace(c));
		switch (c) {
		case 0:
			throw syntaxError("Misshaped meta tag");
		case '<':
			return XMLParser.LT;
		case '>':
			return XMLParser.GT;
		case '/':
			return XMLParser.SLASH;
		case '=':
			return XMLParser.EQ;
		case '!':
			return XMLParser.BANG;
		case '?':
			return XMLParser.QUEST;
		case '"':
		case '\'':
			q = c;
			for (;;) {
				c = next();
				if (c == 0) {
					throw syntaxError("Unterminated string");
				}
				if (c == q) {
					return Boolean.TRUE;
				}
			}
		default:
			for (;;) {
				c = next();
				if (Character.isWhitespace(c)) {
					return Boolean.TRUE;
				}
				switch (c) {
				case 0:
				case '<':
				case '>':
				case '/':
				case '=':
				case '!':
				case '?':
				case '"':
				case '\'':
					back();
					return Boolean.TRUE;
				}
			}
		}
	}

	/**
	 * Get the next XML Token. These tokens are found inside of angle brackets.
	 * It may be one of these characters: <code>/ > = ! ?</code> or it may be a
	 * string wrapped in single quotes or double quotes, or it may be a name.
	 * 
	 * @return a String or a Character.
	 * @throws XMLException
	 *             If the XML is not well formed.
	 */
	public Object nextToken() throws XMLException {
		char c;
		char q;
		StringBuilder sb;
		do {
			c = next();
		} while (Character.isWhitespace(c));
		switch (c) {
		case 0:
			throw syntaxError("Misshaped element");
		case '<':
			throw syntaxError("Misplaced '<'");
		case '>':
			return XMLParser.GT;
		case '/':
			return XMLParser.SLASH;
		case '=':
			return XMLParser.EQ;
		case '!':
			return XMLParser.BANG;
		case '?':
			return XMLParser.QUEST;

		// Quoted string

		case '"':
		case '\'':
			q = c;
			sb = new StringBuilder();
			for (;;) {
				c = next();
				if (c == 0) {
					throw syntaxError("Unterminated string");
				}
				if (c == q) {
					return sb.toString();
				}
				if (c == '&') {
					sb.append(nextEntity(c));
				} else {
					sb.append(c);
				}
			}
		default:

			// Name

			sb = new StringBuilder();
			for (;;) {
				sb.append(c);
				c = next();
				if (Character.isWhitespace(c)) {
					return sb.toString();
				}
				switch (c) {
				case 0:
					return sb.toString();
				case '>':
				case '/':
				case '=':
				case '!':
				case '?':
				case '[':
				case ']':
					back();
					return sb.toString();
				case '<':
				case '"':
				case '\'':
					throw syntaxError("Bad character in a name");
				}
			}
		}
	}

	/**
	 * Skip characters until past the requested string. If it is not found, we
	 * are left at the end of the source with a result of false.
	 * 
	 * @param to
	 *            A string to skip past.
	 * @throws XMLException
	 */
	public boolean skipPast(String to) throws XMLException {
		boolean b;
		char c;
		int i;
		int j;
		int offset = 0;
		int length = to.length();
		char[] circle = new char[length];

		/*
		 * First fill the circle buffer with as many characters as are in the to
		 * string. If we reach an early end, bail.
		 */

		for (i = 0; i < length; i += 1) {
			c = next();
			if (c == 0) {
				return false;
			}
			circle[i] = c;
		}

		/* We will loop, possibly for all of the remaining characters. */

		for (;;) {
			j = offset;
			b = true;

			/* Compare the circle buffer with the to string. */

			for (i = 0; i < length; i += 1) {
				if (circle[j] != to.charAt(i)) {
					b = false;
					break;
				}
				j += 1;
				if (j >= length) {
					j -= length;
				}
			}

			/* If we exit the loop with b intact, then victory is ours. */

			if (b) {
				return true;
			}

			/*
			 * Get the next character. If there isn't one, then defeat is ours.
			 */

			c = next();
			if (c == 0) {
				return false;
			}
			/*
			 * Shove the character in the circle buffer and advance the circle
			 * offset. The offset is mod n.
			 */
			circle[offset] = c;
			offset += 1;
			if (offset >= length) {
				offset -= length;
			}
		}
	}
}
