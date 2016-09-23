package ws.http.tools.json;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ws.http.tools.Cast;

public class QueryToken<T> {

	private static final String EMPTY = "";
	private static final String PATH_DELIMITER_REGEX = "(?<!\\\\)[.]";
	private static final String DOUBLE_BACKSLASH = "\\";
	private static final String QUADRUPLE_BACKSLASH = "\\\\";
	private static final String DOLLARSIGN = "$";
	private static final String DOLLARSIGN_BACKSLASH = "\\$";
	private static final String BACKTICK = "`";
	private static final String DOUBLE_BACKSLASH_BACKTICK = "\\`";
	private static final String DOUBLE_BACKTICK = "``";
	private static final String KEYQUOTES = "``(?!`)|(?<!\\\\)`";
	private static final String KEY_PLACEHOLDER = "``";

	boolean isString = false, isInt = false;
	T value;

	public QueryToken(T value) {
		this.value = value;
		if (value instanceof String) {
			this.isString = true;
		} else {
			this.isInt = true;
		}
	}

	public boolean isString() {
		return this.isString;
	}

	public boolean isInt() {
		return this.isInt;
	}

	public T getValue() {
		return value;
	}

	private static List<String> fragmentOnQuotedKeys(String path) {

		Pattern pattern = Pattern.compile(KEYQUOTES);
		Matcher matcher = pattern.matcher(path);

		List<String> tokens = new ArrayList<String>();

		String lastFragment = EMPTY;
		int index = 0;
		int lastIndex = 0;
		int group = 0;
		int capture_group = 0;
		boolean someGroupIsCaptured = false;

		while (matcher.find()) {
			String groupStr = matcher.group(0);
			if (groupStr.equals(DOUBLE_BACKTICK)) {
				group = 1;
			} else {
				group = 2;
			}
			if (!someGroupIsCaptured || group == capture_group) {
				index = matcher.start();
				String token = path.substring(lastIndex, index);
				tokens.add(token);
				lastIndex = matcher.end();
				capture_group = group;
				someGroupIsCaptured = !someGroupIsCaptured;
				// special case 1 (captured ` found \``)
			} else if (group == 1 && capture_group == 2) {
				index = matcher.start() + 1;
				String token = path.substring(lastIndex, index);
				tokens.add(token);
				lastIndex = index + 1;
				someGroupIsCaptured = !someGroupIsCaptured;
			}
		}
		if (someGroupIsCaptured) {
			return null;
		}
		if (lastIndex < path.length()) {
			lastFragment = path.substring(lastIndex, path.length());
		} else {
			lastFragment = EMPTY;
		}
		tokens.add(lastFragment);
		return tokens;
	}

	public static QueryToken<?>[] parsePath(String path) {

		List<String> tokensList = fragmentOnQuotedKeys(path);
		Object[] tokensStr = tokensList.toArray();

		if (tokensStr == null) {
			return new QueryToken[] {};
		}

		StringBuilder str = new StringBuilder(EMPTY);
		for (int i = 0; i < tokensStr.length; i += 2) {
			str.append((String) tokensStr[i]);
			if (i + 2 < tokensStr.length)
				str.append(KEY_PLACEHOLDER);
		}
		path = str.toString();

		String[] keysString = path.split(PATH_DELIMITER_REGEX);
		QueryToken<?>[] keys = new QueryToken<?>[keysString.length];
		int replacementCount = 1;
		int i = 0;
		boolean isReplacement = false;
		for (Object keyObj : keysString) {
			String key = (String) keyObj;
			while (key.contains(KEY_PLACEHOLDER)) {
				key = key.replaceFirst(KEY_PLACEHOLDER, ((String) tokensStr[replacementCount])
						.replace(DOUBLE_BACKSLASH, QUADRUPLE_BACKSLASH).replace(DOLLARSIGN, DOLLARSIGN_BACKSLASH));
				replacementCount += 2;
				isReplacement = true;
			}
			key = key.replace(DOUBLE_BACKSLASH_BACKTICK, BACKTICK);
			if (Cast.isInteger(key) && !isReplacement) {
				keys[i++] = new QueryToken<Integer>(Integer.valueOf(key));
			} else {
				if (!key.equals("")) keys[i++] = new QueryToken<String>(key);
			}
			isReplacement = false;
		}
		return keys;
	}

}
