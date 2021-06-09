package ws.http.tools.json;

public class Query {

	public static JsonValue find(JsonValue json, String path) {
		
		if ( null == json ) return JSON.NULL;
		if ( null == path || path.equals("") ) return json;
		
		if ( json.isArray() || json.isObject() )
		{
			JsonValue result = json;
			
			QueryToken<?>[] tokens = QueryToken.parsePath(path);
			if ( tokens.length > 0 )
			{
				for (QueryToken<?> token : tokens)
				{
					if (token.isString())
					{
						if ( !result.isObject() ) return JSON.NULL;
						
						result = result.asObject().get((String) token.getValue() );
						
						if ( null == result ) return JSON.NULL;
					}
					else if (token.isInt())
					{
						if ( !result.isArray() ) return JSON.NULL;
						int index = (Integer) token.getValue();
						
						if (index < result.asArray().size())
						{
							result = result.asArray().get(index);
							if ( null == result ) return JSON.NULL;
						}
						else
						{
							return JSON.NULL;
						}
					}
				}
			}
			
			return result;
		}
		
		return json;
	}


}
