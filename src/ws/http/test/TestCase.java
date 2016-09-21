package ws.http.test;

import java.io.IOException;

import ws.http.Request;
import ws.http.Response;
import ws.http.doc.json.JSON;
import ws.http.doc.json.JSONArray;
import ws.http.doc.json.JSONObject;

import java.util.logging.*;

public class TestCase {
	
	private static Logger logger = Logger.getLogger(TestCase.class.getName());
	
	public static void main(String[] args) throws IOException {
		TestCase obj = new TestCase();
//		obj.testGet();
//		obj.testPost();
		obj.testJson();
		
	}
	
	public void testJson()
	{
		logger.info( JSON.toJSON(null) );
//		String json = "{\"account_id\":\"121\",\"channel\":\"ycb\",\"domain\":\"183.131.145.124\",\"port\":\"80\",\"request_id\":\"e54af3f3-d915-4524-8805-3108d78a2220\",\"request_time\":\"1473833813956\",\"timestamp\":1474251393,\"sign\":\"6905d6e800b8bfed60ca02efbe404abb\"}";
//		JSONObject jo = (JSONObject) JSON.parseJSON(json);
//		System.out.println(jo.getStringValue("channel"));
//		logger.info(jo.getStringValue("channel"));
//		logger.info(jo.toString());
		
		Object jo = JSON.parseJSON("[{\"a\":\"21\"}]");
		
		System.out.println(jo instanceof JSONObject);
		
	}
	
	public void testGet() throws IOException
	{
		Response httpResponse = new Request("https://freegeoip.net/csv/8.8.8.8")
                .addHeader("Content-Type", "application/json").getResource();

        String responseBody = httpResponse.getBody();
		
        logger.info(responseBody);
        
	}
	
	public void testPost() throws IOException
	{
		Response httpResponse = new Request("http://portal-qa-b.toushibao.com/api/innerV1/app/getTopoStructure")
                .addHeader("Content-Type", "application/json")
                .setBody("{\"account_id\":\"121\",\"channel\":\"ycb\",\"domain\":\"183.131.145.124\",\"port\":\"80\",\"request_id\":\"e54af3f3-d915-4524-8805-3108d78a2220\",\"request_time\":\"1473833813956\",\"timestamp\":1474251393,\"sign\":\"6905d6e800b8bfed60ca02efbe404abb\"}")
                .postResource();

        String responseBody = httpResponse.getBody();
		logger.info(responseBody);
		
		logger.info(JSON.toJSON(httpResponse));
	}
	
}
