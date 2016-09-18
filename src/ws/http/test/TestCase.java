package ws.http.test;

import java.io.IOException;

import ws.http.Request;
import ws.http.Response;
import java.util.logging.*;

public class TestCase {
	
	private static Logger logger = Logger.getLogger(TestCase.class.getName());
	
	public static void main(String[] args) throws IOException {
		TestCase obj = new TestCase();
		obj.testGet();
		obj.testPost();
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
		Response httpResponse = new Request("http://example.com")
                .addHeader("Content-Type", "application/json")
                .addQueryParameter("foo", "bar")
                .setBody("{foo: 'bar'}")
                .postResource();

        String responseBody = httpResponse.getBody();
		logger.info(responseBody);
	}
	
}
