package ws.http.tools;

import ws.http.tools.json.Json;
import ws.http.tools.json.JsonValue;
import ws.http.tools.json.ToJson;

public abstract class JSON {

	public static JsonValue parseJSON(String text) {
		return Json.parse(text);
	}
	
	public static String toJSON(Object obj)
	{
		if (obj instanceof JsonValue) return obj.toString();
		return ToJson.toJSON(obj);
	}
	
}
