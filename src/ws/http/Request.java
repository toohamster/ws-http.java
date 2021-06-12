package ws.http;

import ws.http.tools.json.JSON;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class represents an HTTP Request message.
 */
public class Request extends Message<Request> {

    /**
     * enable debug to print log
     */
    boolean enableDebug = true;

    HttpURLConnection connection;
    OutputStreamWriter writer;

    URL url;
    Map<String, String> query = new HashMap<String, String>();

    /**
     * ca trust type
     */
    int caTrustType = 0;

    /**
     * Trust the certificate provided by the system
     */
    public final static int CA_TRUST_SYSTEM = 1;

    /**
     * Trust all certificates
     */
    public final static int CA_TRUST_ALL = 2;

    /**
     * Trust the specified certificate
     */
    public final static int CA_TRUST_CUSTOM = 3;

    private final static Logger logger = Logger.getLogger(Request.class.getName());

    /**
     * The Constructor takes the url as a String.
     *
     * @param url The url parameter does not need the query string parameters if
          *       they are going to be supplied via calls to {@link #addQueryParameter(String, String)}.  You can, however, supply
          *       the query parameters in the URL if you wish.
     * @throws IOException
     */
    public Request(String url) throws IOException {
        this(url, true);
    }

    public Request(String url, boolean trustAllSSL) throws IOException {
        this(url, trustAllSSL ? CA_TRUST_ALL : CA_TRUST_SYSTEM, null, null);
    }

    public Request(String url, String sslProtocol) throws IOException {
        this(url, CA_TRUST_ALL, null, sslProtocol);
    }

    public Request(String url, File certificateFile) throws IOException {
        this(url, CA_TRUST_CUSTOM, certificateFile, null);
    }

    public Request(String url, File certificateFile, String sslProtocol) throws IOException {
        this(url, CA_TRUST_CUSTOM, certificateFile, sslProtocol);
    }

    public Request(String url, int caTrustType, File certificateFile, String sslProtocol) throws IOException {

        this.url = new URL(url);

        if (this.url.getProtocol().toLowerCase().equals("https")) {

            SSLContext context = null;

            do {
                // Trust the certificate provided by the system
                if (caTrustType == CA_TRUST_SYSTEM) break;

                if (caTrustType == CA_TRUST_CUSTOM && certificateFile != null) {
                    // Trust the specified certificate
                    context = HTTPSTrustManager.getTrustSSLContext(certificateFile, sslProtocol);
                    // 有可能 协议出错返回 null
                    if (context != null) {
                        break;
                    }

                    caTrustType = CA_TRUST_ALL;
                }

                if (caTrustType == CA_TRUST_ALL) {
                    // Trust all certificates
                    context = HTTPSTrustManager.getTrustAllSSLContext(sslProtocol);
                    // 有可能 协议出错返回 null
                    if (context != null) {
                        break;
                    }

                    caTrustType = CA_TRUST_SYSTEM;
                }

                // 自动切换成 信任系统提供的证书

            } while (false);

            switch (caTrustType) {
                case CA_TRUST_CUSTOM:
                    connection = (HttpsURLConnection) this.url.openConnection();
                    ((HttpsURLConnection) connection).setSSLSocketFactory(context.getSocketFactory());
                    break;
                case CA_TRUST_ALL:
                    HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
                    connection = (HttpsURLConnection) this.url.openConnection();
                    break;
                default:
                    connection = (HttpsURLConnection) this.url.openConnection();
                    break;
            }
            this.caTrustType = caTrustType;
        } else {
            connection = (HttpURLConnection) this.url.openConnection();
        }

        // 禁用缓存
        connection.setUseCaches(false);
    }

    /**
     * Sets the connect timeout & read timeout
     *
     * @param connectTimeout an int that specifies the connect timeout value to be used in milliseconds
     * @param readTimeout   an int that specifies the read timeout value to be used in milliseconds
     * @return this Request, to support chained method calls
     * @Throws IllegalArgumentException – if the timeout parameter is less than 0
     */
    public Request setTimeout(int connectTimeout, int readTimeout)
    {
        // 设置连接超时时间
        connection.setConnectTimeout(connectTimeout);
        // 设置读取超时时间
        connection.setReadTimeout(readTimeout);
        return this;
    }

    /**
     * Adds a Query Parameter to a list.  The list is converted to a String and appended to the URL when the Request
     * is submitted.
     *
     * @param name  The Query Parameter's name
     * @param value The Query Parameter's value
     * @return this Request, to support chained method calls
     */
    public Request addQueryParameter(String name, String value) {
        this.query.put(name, value);
        return this;
    }

    /**
     * Removes the specified Query Parameter.
     *
     * @param name The name of the Query Parameter to remove
     * @return this Request, to support chained method calls
     */
    public Request removeQueryParameter(String name) {
        this.query.remove(name);
        return this;
    }

    /**
     * Issues a GET to the server.
     * @return The {@link Response} from the server
     * @throws IOException
     */
    public Response getResource() throws IOException {
        buildQueryString();
        buildHeaders();

        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        return readResponse(null);
    }

    /**
     * Issues a PUT to the server.
     * @return The {@link Response} from the server
     * @throws IOException
     */
    public Response putResource() throws IOException {
        return writeResource("PUT", this.body);
    }

    /**
     * Issues a POST to the server.
     * @return The {@link Response} from the server
     * @throws IOException
     */
    public Response postResource() throws IOException {
        return writeResource("POST", this.body);
    }

    /**
     * Issues a DELETE to the server.
     * @return The {@link Response} from the server
     * @throws IOException
     */
    public Response deleteResource() throws IOException {
        buildQueryString();
        buildHeaders();

        connection.setDoOutput(true);
        connection.setRequestMethod("DELETE");

        return readResponse(null);
    }

    /**
     * A private method that handles issuing POST and PUT requests
     *
     * @param method POST or PUT
     * @param body The body of the Message
     * @return the {@link Response} from the server
     * @throws IOException
     */
    private Response writeResource(String method, String body) throws IOException {
        buildQueryString();
        buildHeaders();

        connection.setDoOutput(true);
        connection.setRequestMethod(method);

        writer = new OutputStreamWriter(connection.getOutputStream());
        writer.write(body);
        writer.close();

        return readResponse(body);
    }

    private static void debugRequest(Request request, String requestBody)
    {
        StringBuilder builder = new StringBuilder();
        String newline = System.getProperty("line.separator");

        builder.append(request.connection.getRequestMethod())
                .append(" ")
                .append(request.url.getProtocol())
                .append("://")
                .append(request.url.getHost())
                .append(request.url.getPort() == -1 ? "" : ":" + request.url.getPort())
                .append(request.url.getPath())
                .append(newline)
                .append("Params: ")
                .append(request.url.getQuery())
                .append(newline)
                .append("CATrustType: ").append(request.caTrustType)
                .append(newline)
                .append("Headers: ").append(newline);

        for (Map.Entry<String, List<String>> entry : request.headers.entrySet()) {
            List<String> values = entry.getValue();
            for (String value : values) {
                builder.append(entry.getKey()).append(" = ").append(value).append(newline);
            }
        }

        if (requestBody != null) {
            builder.append(newline).append("Body: ").append(newline)
                    .append(requestBody);
        }

        logger.info(builder.toString());
    }

    private static void debugResponse(Response response)
    {
        logger.info(response.toString());
    }

    /**
     * A private method that handles reading the Responses from the server.
     *
     * @param requestBody
     * @return a {@link Response} from the server.
     * @throws IOException
     */
    private Response readResponse(String requestBody) throws IOException {

        if (this.enableDebug) {
            debugRequest(this, requestBody);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        reader.close();

        Response response = new Response()
                .setResponseCode(connection.getResponseCode())
                .setResponseMessage(connection.getResponseMessage())
                .setHeaders(connection.getHeaderFields())
                .setBody(builder.toString());

        if (this.enableDebug) {
            debugResponse(response);
        }

        return response;
    }

    /**
     * A private method that loops through the query parameter Map, building a String to be appended to the URL.
     *
     * @throws MalformedURLException
     */
    @SuppressWarnings("rawtypes")
	private void buildQueryString() throws MalformedURLException {
        StringBuilder builder = new StringBuilder();

        // Put the query parameters on the URL before issuing the request
        if (!query.isEmpty()) {
            for (Map.Entry param : query.entrySet()) {
                builder.append(param.getKey());
                builder.append("=");
                builder.append(param.getValue());
                builder.append("&");
            }
            builder.deleteCharAt(builder.lastIndexOf("&")); // Remove the trailing ampersand
        }

        if (builder.length() > 0) {
            // If there was any query string at all, begin it with the question mark
            builder.insert(0, "?");
        }

        url = new URL(url.toString() + builder.toString());
    }

    /**
     * A private method that loops through the headers Map, putting them on the Request or Response object.
     */
    private void buildHeaders() {
        if (!headers.isEmpty()) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                List<String> values = entry.getValue();

                for (String value : values) {
                    connection.addRequestProperty(entry.getKey(), value);
                }
            }
        }

    }

}

