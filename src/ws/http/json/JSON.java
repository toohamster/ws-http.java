package ws.http.json;

public class JSON {
	
	public static String toJSON(Object obj)
	{
		return ObjectTo.toJSON(obj);
	}
	
	public static Object parseJSON(String json) {
		return json == null || json.isEmpty() ? null : parseJSON(json);
	}
	
}
