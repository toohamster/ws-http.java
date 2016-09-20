package ws.http.json;

import java.util.HashMap;
import java.util.Stack;

public class ToObject {
	
	public static Object parse(String text) {
		return (null == text || text.isEmpty()) ? null : parseJSON(text);
	}

	public static <T extends Object> T parse(String text, Class<T> clazz) {
		if (null == text || text.isEmpty() || null == clazz || clazz.isInterface() || !isJSONObject(text))
			return null;
		return CaseType.objectValue(parseJSONObject(text), clazz);
	}

	public static JSONArray parseJSONArray(String text) {
		return ((null == text || text.isEmpty()) || !isJSONArray(text)) ? null : JSONArray.class.cast(parseJSON(text));
	}

	public static JSONObject parseJSONObject(String text) {
		return ((null == text || text.isEmpty()) || !isJSONObject(text)) ? null : JSONObject.class.cast(parseJSON(text));
	}	

	private static boolean isJSONObject(String text) {
		text = text.trim();
		return text.startsWith("{") && text.endsWith("}");
	}

	private static boolean isJSONArray(String text) {
		text = text.trim();
		return text.startsWith("[") && text.endsWith("]");
	}

	private static Object parseJSON(String text) {
		text.trim();
		Stack<String> stack = new Stack<String>();
		Stack<Object> objects = new Stack<Object>();
		Stack<String> keys = new Stack<String>();
		Stack<Boolean> types = new Stack<Boolean>();
		int idx = 0;
		int pos = 0;
		Object obj = null;
		do {
			switch (text.charAt(idx++)) {
			case '[':
			case '{':
				boolean isMap = text.charAt(idx - 1) == '{';
				if (stack.size() != 0 && stack.lastElement().equals("\"")) {
					break;
				}
				stack.push("}");
				types.push(isMap);
				if (null != obj)
					objects.push(obj);
				
				if ( isMap )
				{
					obj = obtain(JSONObject.class);
				}
				else
				{
					obj = obtain(JSONArray.class);
				}
				
				pos = idx;
				break;
			case ']':
			case '}':
				if (pos != idx) {
					String value = text.substring(pos, idx - (text.charAt(idx - 2) == '"' ? 2 : 1)).trim();
					if (!value.isEmpty() && obj instanceof JSONObject) {
						JSONObject.class.cast(obj).put(keys.pop(), value);
					} else if (!value.isEmpty() && obj instanceof JSONArray) {
						JSONArray.class.cast(obj).add(value);
					}
				}
				types.pop();
				stack.pop();
				if (objects.size() > 0) {
					if (objects.lastElement() instanceof HashMap) {
						JSONObject.class.cast(objects.lastElement()).put(keys.pop(), obj);
					} else {
						JSONArray.class.cast(objects.lastElement()).add(obj);
					}
					obj = objects.pop();
				}
				pos = idx + 1;
				break;
			case '"':
				if (stack.lastElement().equals("\"")) {
					stack.pop();
				} else {
					stack.push("\"");
					pos = idx;
				}
				break;
			case ',':
				if (pos != idx) {
					String value = text.substring(pos, idx - (text.charAt(idx - 2) == '"' ? 2 : 1)).trim();
					if (types.lastElement()) {
						JSONObject.class.cast(obj).put(keys.pop(), value);
					} else {
						JSONArray.class.cast(obj).add(value);
					}
				}
				pos = idx;
				break;
			case ':':
				if (text.charAt(idx - 2) == '"')
					keys.push(text.substring(pos, (pos = idx) - 2));
				break;
			}
		} while (idx < text.length());
		return obj;
	}

	private static <T extends Object> T obtain(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}
