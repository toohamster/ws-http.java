package ws.http.tools.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.xml.sax.InputSource;

import ws.http.tools.json.JSON;
import ws.http.tools.json.JsonArray;
import ws.http.tools.json.JsonObject;
import ws.http.tools.json.JsonValue;

public class XML {
	
	public static String toXML(Object value, String tag)
	{
		if (value instanceof JsonValue)
		{
			return prettyXml(jsonToXML((JsonValue) value, tag));
		}
		// 
		value = JSON.toJSON(value);
		value = JSON.parseJSON((String) value);
		return prettyXml(jsonToXML((JsonValue) value, tag));
	}
	
	private static String jsonToXML(JsonValue json, String tag)
	{
		if ( null == json ) return "";
		
		if ( json.isObject() )
		{
			StringBuilder sb = new StringBuilder();
			
			if (tag != null) {
	            sb.append('<');
	            sb.append(tag);
	            sb.append('>');
	        }
			
			JsonObject jo = json.asObject();
			
			Iterator<String> keys = jo.names().iterator();
			
			while (keys.hasNext()) {
				String key = keys.next();
				JsonValue val = jo.get(key);
				
				if (val.isArray())
				{
					if ("values".equals(key))
					{
						sb.append(jsonToXML(val,null));
					}
					else
					{
						sb.append(jsonToXML(val, key));
					}
				}
				else
				{
					sb.append(jsonToXML(val, key));
				}
				
//				sb.append(jsonToXML(val, key));
			}
			
			if (tag != null) {
                sb.append("</");
                sb.append(tag);
                sb.append('>');
            }
			
            return sb.toString();
		}
		
		if (json.isArray())
		{
			StringBuilder sb = new StringBuilder();
			
			JsonArray ja = json.asArray();
			
			for (JsonValue val : ja.values()) {
				
				if (val.isArray() || val.isObject()) {
					sb.append(jsonToXML(val, tag));
				}
				else
				{
					if (tag != null) {
			            sb.append('<');
			            sb.append(tag);
			            sb.append('>');
			        }
					sb.append(jsonToXML(val, null));
					if (tag != null) {
		                sb.append("</");
		                sb.append(tag);
		                sb.append('>');
		            }
				}
				
				
			}
			
			return sb.toString();
		}
		
		// Number/String/Literal/
		String val = "";
		
		if (json.isString())
		{
			val = json.asString();
			if (val.length() > 0)
			{
				val = "<![CDATA[" + val + "]]>";
			}
		}
		else
		{
			val = json.toString();
		}
		
		if (tag == null) return val;
		
		if (val.length() == 0)
		{
			return "<" + tag + "/>";
		}
		
		return "<" + tag + ">" + val + "</" + tag + ">";
	}
	
	private static String prettyXml(String xml){
        try{
            Transformer serializer= SAXTransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.STANDALONE, "yes");
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            Source xmlSource=new SAXSource(new InputSource(new ByteArrayInputStream(xml.getBytes())));
            StreamResult res =  new StreamResult(new ByteArrayOutputStream());            
            serializer.transform(xmlSource, res);
            return new String(((ByteArrayOutputStream)res.getOutputStream()).toByteArray());
        }catch(Exception e){
            return xml;
        }
    }
	
	public static XmlValue parseXML(String string) {
		
		return null;
	}
	
	
	public static XmlValue query() {
		
		return null;
	}
	
}
