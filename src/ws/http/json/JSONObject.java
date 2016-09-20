package ws.http.json;

import java.util.HashMap;

public class JSONObject extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	public JSONObject() {

	}

	public Integer getIntValue(String k) {
		return CaseType.intValue(get(k));
	}

	public Integer getIntValue(String k, Integer value) {
		Integer t = getIntValue(k);
		return null == t ? value : t;
	}

	public Long getLongValue(String k) {
		return CaseType.longValue(k);
	}

	public Long getLongValue(String k, Long value) {
		Long t = getLongValue(k);
		return null == t ? value : t;
	}

	public Short getShortValue(String k) {
		return CaseType.shortValue(get(k));
	}

	public Short getShortValue(String k, Short value) {
		Short t = getShortValue(k);
		return null == t ? null : t;
	}

	public Float getFloatValue(String k) {
		return CaseType.floatValue(get(k));
	}

	public Float getFloatValue(String k, Float value) {
		Float t = getFloatValue(k);
		return null == t ? null : t;
	}

	public Double getDoubleValue(String k) {
		return CaseType.doubleValue(get(k));
	}

	public Double getDoubleValue(String k, Double value) {
		Double t = getDoubleValue(k);
		return null == t ? null : t;
	}

	public Byte getByteValue(String k) {
		return CaseType.byteValue(get(k));
	}

	public Byte getByteValue(String k, Byte value) {
		Byte t = getByteValue(k);
		return null == t ? null : t;
	}

	public Boolean getBoolValue(String k) {
		return CaseType.booleanValue(get(k));
	}

	public Boolean getBoolValue(String k, Boolean value) {
		Boolean t = getBoolValue(k);
		return null == t ? null : t;
	}

	public Character getCharValue(String k) {
		return CaseType.charValue(get(k));
	}

	public Character getCharValue(String k, Character value) {
		Character t = getCharValue(k);
		return null == t ? null : t;
	}

	public String getStringValue(String k) {
		return CaseType.stringValue(get(k));
	}

	public String getStringValue(String k, String value) {
		String t = getStringValue(k);
		return null == t ? null : t;
	}
	
	public <T> T getPojoValue(String k,Class<T> clazz) {
		return CaseType.objectValue(getJSONObject(k), clazz);
	}

	public JSONObject getJSONObject(String k) {
		Object obj = get(k);
		return obj instanceof JSONObject ? JSONObject.class.cast(obj) : null;
	}

	public JSONObject getJSONObject(String k, JSONObject value) {
		JSONObject t = getJSONObject(k);
		return null == t ? null : t;
	}

	public JSONArray getJSONArray(String k) {
		Object obj = get(k);
		return obj instanceof JSONArray ? JSONArray.class.cast(obj) : null;
	}

	public JSONArray getJSONArray(String k, JSONArray value) {
		JSONArray t = getJSONArray(k);
		return null == t ? null : t;
	}

	public String toJSONString() {
		return JSON.toJSON(this);
	}

	public <T> T toPojo(Class<T> clazz) {
		return CaseType.objectValue(this, clazz);
	}
	
	public String toString() {
		return toJSONString();
	}

}
