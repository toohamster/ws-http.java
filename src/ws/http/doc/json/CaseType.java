package ws.http.doc.json;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseType {
	public static Integer intValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.intValue();
	}

	public static Long longValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.longValue();
	}

	public static Short shortValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.shortValue();
	}

	public static Float floatValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.floatValue();
	}

	public static Double doubleValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.doubleValue();
	}

	public static Byte byteValue(Object value) {
		Number number = caseNumber(value);
		return null == number ? null : number.byteValue();
	}

	public static Character charValue(Object value) {
		if (null != value && isPrimitive(value.getClass())) {
			return value.toString().charAt(0);
		}
		return null;
	}

	public static String stringValue(Object value) {
		if (null != value && isPrimitive(value.getClass())) {
			return value.toString();
		}
		return null;
	}

	public static Boolean booleanValue(Object value) {
		if (null != value && isPrimitive(value.getClass())) {
			return Boolean.valueOf(value.toString());
		}
		return false;
	}

	public static <T extends Object> T objectValue(JSONObject value, Class<T> clazz) {
		if (null == value || null == clazz || clazz.isInterface())
			return null;
		Method[] methods = clazz.getMethods();
		T result = obtain(clazz);
		for (Method method : methods) {
			if (!method.getName().startsWith("set") || method.getParameterTypes().length > 1)
				continue;
			String k = String.format("%s%s", Character.toLowerCase(method.getName().charAt(3)),
					method.getName().substring(4));
			if (!value.containsKey(k))
				continue;
			Class<?> targetClazz = method.getParameterTypes()[0];
			try {
				Method method2 = CaseType.class.getMethod(caseMethod(targetClazz), Object.class);
				if (null == method2)
					continue;
				Object args = method2.invoke(null, value.get(k));
				if (null != args) {
					method.invoke(result, args);
				}
			} catch (Exception e) {
				continue;
			}
		}
		return result;
	}

	public static boolean isPrimitive(Class<?> clazz) {
		return null != clazz && (Number.class.isAssignableFrom(clazz) || Pattern.matches(
				"^(java\\.lang\\.)?([ilfsSdbBcC](nt|ong|loat|hort|ouble|oolean|yte|har(acter)?|tring))$",
				clazz.getName()));
	}

	private static Number caseNumber(Object value) {
		if (null != value && isPrimitive(value.getClass())) {
			Matcher matcher = Pattern.compile("[0-9\\.]+").matcher(value.toString());
			if (matcher.find()) {
				return Double.valueOf(matcher.group(0));
			}
		}
		return null;
	}

	private static String caseMethod(Class<?> clazz) {
		if (isPrimitive(clazz)) {
			String temp = clazz.getSimpleName();
			if (temp.equals("Integer")) {
				temp = "int";
			} else if (temp.equals("Character")) {
				temp = "char";
			}
			return String.format("%s%sValue", Character.toLowerCase(temp.charAt(0)), temp.substring(1));
		}
		return null;
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
