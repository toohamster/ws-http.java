package ws.http.test;

import java.io.IOException;

import ws.http.Request;
import ws.http.Response;
import ws.http.tools.json.JSON;
import ws.http.tools.json.JsonValue;
import ws.http.tools.xml.XML;

import java.util.ArrayList;
import java.util.HashMap;
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
		JsonValue jsonobj = null;
//		logger.info( JSON.stringify(null) );
//		logger.info( JSON.NULL.toString() );
//		String json = "{\"account_id\":121,\"channel\":{\"a\":[6,6,\"jj\"]},\"domain\":\"183.131.145.124\",\"port\":\"80\",\"request_id\":\"e54af3f3-d915-4524-8805-3108d78a2220\",\"request_time\":\"1473833813956\",\"timestamp\":1474251393,\"sign\":\"6905d6e800b8bfed60ca02efbe404abb\"}";
//		jsonobj =  JSON.parseJSON(json);
////		
//		System.out.println(XML.stringify(json));
////		System.out.println(XML.stringify(jsonobj));
		
//		System.out.println(jsonobj.asObject().get("channel"));
//		
//		System.out.println(jsonobj.asObject().get("request_id").asString());
//		
//		System.out.println(JSON.query(jsonobj, "request_id").asString());
////		System.out.println( JSON.query(jsonobj, "sign").asString() );
//
////		logger.info( JSON.object().add("list", JSON.array().add(JSON.object().add("id", 123).add("age", 456))).toString() );
//				
		jsonobj = JSON.parseJSONFile("C:/tmptt/postmanv1.json", false);
//		System.out.println(XML.stringify(jsonobj,"postmanv1"));
		
		String postman2xml = XML.stringify(jsonobj);
		
//		HashMap<?, ?> xmlobj = XML.parseXMLFile("C:/tmptt/postmanv1.xml", false);
		HashMap<?, ?> xmlobj = XML.parseXML(postman2xml);
		System.out.println(JSON.stringify(xmlobj));
//		System.out.println( XML.stringify(a, "doc"));
//		logger.info(jsonobj.toString());
//		System.out.println(JSON.query(jsonobj, "id").asString());
//		System.out.println(JSON.query(jsonobj, "description").asString());
//		System.out.println(JSON.query(jsonobj, "folders.1.name"));
//		System.out.println(JSON.query(jsonobj, "requests.0.responses.0.headers.2.key").asString());
//		logger.info(jsonobj.asObject().get("id").toString());
	}
	
	public void testGet() throws IOException
	{
		Response httpResponse = new Request("http://freegeoip.net/csv/8.8.8.8")
                .addHeader("Content-Type", "application/json").getResource();

        String responseBody = httpResponse.getBody();
		
        logger.info(JSON.stringify(responseBody));
        
	}
	
	public void testPost() throws IOException
	{
		Response httpResponse = new Request("http://portal-qa-b.toushibao.com/api/innerV1/app/getTopoStructure")
                .addHeader("Content-Type", "application/json")
                .setBody("{\"account_id\":\"121\",\"channel\":\"ycb\",\"domain\":\"183.131.145.124\",\"port\":\"80\",\"request_id\":\"e54af3f3-d915-4524-8805-3108d78a2220\",\"request_time\":\"1473833813956\",\"timestamp\":1474251393,\"sign\":\"6905d6e800b8bfed60ca02efbe404abb\"}")
                .postResource();

        String responseBody = httpResponse.getBody();
		logger.info(responseBody);
		
//		logger.info(JSON.stringify(httpResponse));
	}
	
}
