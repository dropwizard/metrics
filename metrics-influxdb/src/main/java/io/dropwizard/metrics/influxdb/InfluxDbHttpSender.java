package io.dropwizard.metrics.influxdb;

import io.dropwizard.metrics.influxdb.data.InfluxDbPoint;
import io.dropwizard.metrics.influxdb.data.InfluxDbWriteObject;
import io.dropwizard.metrics.influxdb.utils.InfluxDbWriteObjectSerializer;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An implementation of InfluxDbSender that writes to InfluxDb via http.
 */
public class InfluxDbHttpSender implements InfluxDbSender {
    private final CloseableHttpClient closeableHttpClient;
    private final URL url;
    private final String username;
    private final String password;
    private final InfluxDbWriteObject influxDbWriteObject;
    private final InfluxDbWriteObjectSerializer influxDbWriteObjectSerializer;

    /**
     * Creates a new http sender given connection details.
     *
     * @param hostname   the influxDb hostname
     * @param port       the influxDb http port
     * @param database   the influxDb database to write to
     * @param authString the authorization string to be used to connect to InfluxDb, of format username:password
     * @throws Exception exception while creating the influxDb sender(MalformedURLException)
     */
    public InfluxDbHttpSender(final String hostname, final int port, final String database, final String authString) throws Exception {
        this(hostname, port, database, authString, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new http sender given connection details.
     *
     * @param hostname      the influxDb hostname
     * @param port          the influxDb http port
     * @param database      the influxDb database to write to
     * @param authString    the authorization string to be used to connect to InfluxDb, of format username:password
     * @param timePrecision the time precision of the metrics
     * @throws Exception exception while creating the influxDb sender(MalformedURLException)
     */
    public InfluxDbHttpSender(final String hostname, final int port, final String database, final String authString,
                              final TimeUnit timePrecision) throws Exception {
        this.url = new URL("http", hostname, port, "/write");
        this.closeableHttpClient = HttpClients.createDefault();
        //this is a bit ugly - would be nicer to configure "structured", e.g. username/pw as input
        if (authString != null && !authString.isEmpty()) {
            String[] parts = authString.split( ":" );
            this.username = parts[0];
            this.password = parts[1];
        } else {
            this.username = null;
            this.password = null;
        }

        this.influxDbWriteObject = new InfluxDbWriteObject(database, timePrecision);
        this.influxDbWriteObjectSerializer = new InfluxDbWriteObjectSerializer();
    }

    @Override
    public void flush() {
        influxDbWriteObject.setPoints(new HashSet<InfluxDbPoint>());
    }

    @Override
    public boolean hasSeriesData() {
        return influxDbWriteObject.getPoints() != null && !influxDbWriteObject.getPoints().isEmpty();
    }

    @Override
    public void appendPoints(final InfluxDbPoint point) {
        if (point != null) {
            influxDbWriteObject.getPoints().add(point);
        }
    }

    private RequestConfig getRequestConfig() {
        return RequestConfig
            .custom()
            .setConnectTimeout(1000)
            .setConnectionRequestTimeout(1000)
            .build();
    }

    private HttpClientContext getHttpClientContext() {
        HttpClientContext httpClientContext = null;
        if (username != null && !username.isEmpty() && password != null && !password.isEmpty())
        {
            httpClientContext = HttpClientContext.create();
            AuthScope authScope = new AuthScope(url.getHost(), url.getPort());
            UsernamePasswordCredentials usernamePasswordCredentials = new UsernamePasswordCredentials(username, password);
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(authScope, usernamePasswordCredentials);
            httpClientContext.setCredentialsProvider(credentialsProvider);
        }

        return httpClientContext;
    }

    @Override
    public int writeData() throws Exception {
        final String json = influxDbWriteObjectSerializer.getJsonString(influxDbWriteObject);

        HttpPost httpPost = new HttpPost(this.url.toURI());
        httpPost.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON));

        httpPost.setConfig(getRequestConfig());

        Integer responseCode = closeableHttpClient.execute(httpPost, new ResponseHandler<Integer>()
        {
            @Override
            public Integer handleResponse(HttpResponse httpResponse)
                throws ClientProtocolException, IOException
            {
                int statusCode = httpResponse.getStatusLine().getStatusCode();
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (statusCode >= 200 && statusCode < 300) {
                    return statusCode;
                }
                else {
                    throw new ClientProtocolException("Server returned HTTP response code: " + statusCode
                                                          + "for URL: " + url
                                                          + " with content :'"
                                                          + httpResponse.getStatusLine().getReasonPhrase() + "'" );
                }

            }
        }, getHttpClientContext());

        return responseCode;
    }

    @Override
    public void setTags(final Map<String, String> tags) {
        if (tags != null) {
            influxDbWriteObject.setTags(tags);
        }
    }
}
