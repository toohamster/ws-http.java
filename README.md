# ws-http.java
A tiny lib for java httpclient to my girlfriend.

```java
// Issues a GET to Google
Response httpResponse = new Request("http://example.com")
        .getResource();

String responseBody = httpResponse.getBody();

// 
Response httpResponse = new Request("http://example.com")
        .addHeader("x-my-header", "foobar")
        .addQueryParameter("foo", "bar")
        .getResource();

String responseBody = httpResponse.getBody();

// Posts a simple JSON object to the server
Response httpResponse = new Request("http://example.com")
        .addHeader("x-my-header", "foobar")
        .addQueryParameter("foo", "bar")
        .setBody("{foo: 'bar'}")
        .postResource();

String responseBody = httpResponse.getBody();

// parse json string and use quick Query json node
JsonValue jsonobj = JSON.parseJSONFile("C:/tmptt/postmanv1.json", false);
System.out.println(jsonobj.toString());
System.out.println(JSON.query(jsonobj, "id").asString());
System.out.println(JSON.query(jsonobj, "description").asString());
System.out.println(JSON.query(jsonobj, "folders.1.name"));
System.out.println(JSON.query(jsonobj, "requests.0.responses.0.headers.2.key").asString());
System.out.println(jsonobj.asObject().get("id").toString());

```

### Problems

- cannot support https