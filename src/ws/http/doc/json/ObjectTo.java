package ws.http.doc.json;

import java.lang.reflect.Array;  
import java.lang.reflect.Field;  
import java.lang.reflect.Modifier;  
import java.util.Collection;  
import java.util.HashMap;  
import java.util.Iterator;  
import java.util.Map; 

public class ObjectTo {
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String toJSON(Object value) {
		if (value == null)
			return "null";

		if (value instanceof Number)
			return value.toString();
		if (value instanceof Boolean)
			return value.toString();

		if (value instanceof String)
			return "\"" + escape((String) value) + "\"";

		if (value instanceof Double) {
			if (((Double) value).isInfinite() || ((Double) value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if (value instanceof Float) {
			if (((Float) value).isInfinite() || ((Float) value).isNaN())
				return "null";
			else
				return value.toString();
		}

		if (value instanceof Map)
			return map2Json((Map) value);

		if (value instanceof Collection)
			return coll2Json((Collection) value);

		if (value.getClass().isArray())
			return array2Json(value);

		return pojo2Json(value);
	}

	static String array2Json(Object array) {
		if (null == array)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append('[');

		// �˴���1��Ϊ������� ���� ׷��
		int len = Array.getLength(array) - 1;
		if (len > -1) {
			int i;
			for (i = 0; i < len; i++) {
				sb.append(toJSON(Array.get(array, i))).append(", ");
			}
			sb.append(toJSON(Array.get(array, i)));
		}

		sb.append(']');
		return sb.toString();
	}

	static String coll2Json(Collection<?> coll) {
		if (null == coll)
			return "null";
		StringBuffer sb = new StringBuffer();
		sb.append('[');
		for (Iterator<?> it = coll.iterator(); it.hasNext();) {
			sb.append(toJSON(it.next()));
			if (it.hasNext())
				sb.append(", ");
		}
		sb.append(']');
		return sb.toString();
	};

	static String pojo2Json(Object obj) {
		Class<?> type = obj.getClass();
		Field[] fields = type.getDeclaredFields();
		Map<String, Object> map = new HashMap<String, Object>();
		for (Field f : fields) {
			if (Modifier.isStatic(f.getModifiers()))
				continue;
			String name = f.getName();
			f.setAccessible(true);
			Object value = null;
			try {
				value = f.get(obj);
			} catch (Exception e) {
				value = null;
			}
			map.put(name, value);
		}
		type = null;
		fields = null;
		return map2Json(map);
	}

	@SuppressWarnings("rawtypes")
	static String map2Json(Map<String, Object> map) {
		if (null == map)
			return "null";

		StringBuffer sb = new StringBuffer();
		sb.append('{');
		for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();

			String key = (String) entry.getKey();
			if (null == key)
				continue;

			sb.append('\"');
			escape(key, sb);
			sb.append('\"').append(':').append(toJSON(entry.getValue()));

			if (it.hasNext())
				sb.append(", ");
		}
		sb.append('}');
		return sb.toString();
	};

	/**
	 * Escape quotes, \, /, \r, \n, \b, \f, \t and other control characters
	 * (U+0000 through U+001F).
	 * 
	 * @param s
	 * @return
	 */
	public static String escape(String s) {
		if (s == null)
			return null;
		StringBuffer sb = new StringBuffer();
		escape(s, sb);
		return sb.toString();
	}

	/**
	 * @param s - Must not be null.
	 * @param sb
	 */
	static void escape(String s, StringBuffer sb) {
		for (int i = 0; i < s.length(); i++) {
			char ch = s.charAt(i);
			switch (ch) {
			case '"':
				sb.append("\\\"");
				break;
			case '\\':
				sb.append("\\\\");
				break;
			case '\b':
				sb.append("\\b");
				break;
			case '\f':
				sb.append("\\f");
				break;
			case '\n':
				sb.append("\\n");
				break;
			case '\r':
				sb.append("\\r");
				break;
			case '\t':
				sb.append("\\t");
				break;
			case '/':
				sb.append("\\/");
				break;
			default:
				// Reference: http://www.unicode.org/versions/Unicode5.1.0/
				if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F')
						|| (ch >= '\u2000' && ch <= '\u20FF')) {
					String ss = Integer.toHexString(ch);
					sb.append("\\u");
					for (int k = 0; k < 4 - ss.length(); k++) {
						sb.append('0');
					}
					sb.append(ss.toUpperCase());
				} else {
					sb.append(ch);
				}
			}
		}
	}
}
