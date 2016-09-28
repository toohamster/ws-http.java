package ws.http.tools.xml;

import java.util.Iterator;

import ws.http.tools.json.JSON;
import ws.http.tools.json.JsonArray;
import ws.http.tools.json.JsonObject;
import ws.http.tools.json.JsonValue;

public class XML {
	
	public static String toXML(Object value, String tag)
	{
		if (value instanceof JsonValue)
		{
			return jsonToXML((JsonValue) value, tag);
		}
		// 进行1次反转
		value = JSON.toJSON(value);
		value = JSON.parseJSON((String) value);
		return jsonToXML((JsonValue) value, tag);
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
				sb.append(toXML(val, key));
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
				
				if (tag != null) {
		            sb.append('<');
		            sb.append(tag);
		            sb.append('>');
		        }
				
				if (val.isArray() || val.isObject()) {
					sb.append(toXML(val, tag));
				}
				else
				{
					sb.append(toXML(val, null));
				}
				
				if (tag != null) {
	                sb.append("</");
	                sb.append(tag);
	                sb.append('>');
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
	
	public static XmlValue parseXML(String string) {
		
		return null;
	}
	
	
	public static XmlValue query() {
		
		return null;
	}
	
}
