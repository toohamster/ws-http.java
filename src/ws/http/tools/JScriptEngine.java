package ws.http.tools;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * JScriptEngine
 * 
 * @see http://winterbe.com/posts/2014/04/05/java8-nashorn-tutorial/
 * @see http://codereview.stackexchange.com/questions/90272/java-json-parsing-with-the-nashorn-api
 */
public class JScriptEngine {
	
	private ScriptEngineManager engineManager;
	private ScriptEngine engine;
	
	private static JScriptEngine instance = null;
	
	private JScriptEngine()
	{
		this.engineManager = new ScriptEngineManager();
		this.engine = this.engineManager.getEngineByName("nashorn");
		if (this.engine == null)
		{
			this.engine = this.engineManager.getEngineByExtension("js");
		}
	}
	
	public static JScriptEngine getInstance()
	{
		if (instance == null)
		{
			instance = new JScriptEngine();
		}
		
		return instance;
	}
	
	public Object require(String path) throws ScriptException
	{
		if (null != path)
		{
			String script = File.readFromPath(path);
			if (null != script)
			{
				return engine.eval(script);
			}
		}
		return null;
	}
	
	public Object eval(String script) throws ScriptException
	{
		return engine.eval(script);
	}
	
	public Object call(String objName, String method, Object... args) throws NoSuchMethodException, ScriptException
	{
		Invocable invocable = (Invocable) engine;
		Object obj = engine.get(objName);
		if (obj != null) return invocable.invokeMethod(obj, method, args);
		return null;
	}
	
	public Object call(String function, Object... args) throws NoSuchMethodException, ScriptException
	{
		Invocable invocable = (Invocable) engine;		
		return invocable.invokeFunction(function, args);
	}

	public ScriptEngine getEngine() {
		return engine;
	}
	
}
