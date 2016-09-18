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

```

### Problems

- cannot support https