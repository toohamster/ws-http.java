package ws.http.doc.json;

public class JSON {
	
	public static String toJSON(Object obj)
	{
		return ObjectTo.toJSON(obj);
	}

	public static Object parseJSON(String text) {
		return (null == text || text.isEmpty()) ? null : ToObject.parse(text);
	}

	
}
