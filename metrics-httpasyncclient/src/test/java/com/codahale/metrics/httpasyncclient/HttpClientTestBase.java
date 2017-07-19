package com.codahale.metrics.httpasyncclient;

import org.apache.http.HttpHost;
import org.apache.http.impl.nio.bootstrap.HttpServer;
import org.apache.http.impl.nio.bootstrap.ServerBootstrap;
import org.apache.http.nio.protocol.BasicAsyncRequestHandler;
import org.apache.http.nio.reactor.ListenerEndpoint;
import org.apache.http.protocol.HttpRequestHandler;
import org.junit.After;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.concurrent.TimeUnit;

public abstract class HttpClientTestBase {

    /**
     * {@link HttpRequestHandler} that responds with a {@code 200 OK}.
     */
    public static HttpRequestHandler STATUS_OK = (request, response, context) -> response.setStatusCode(200);

    private HttpServer server;

    /**
     * @return A free local port or {@code -1} on error.
     */
    public static int findAvailableLocalPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * Start a local server that uses the {@code handler} to handle requests.
     * <p>
     * The server will be (if started) terminated in the {@link #tearDown()} {@link After} method.
     *
     * @param handler The request handler that will be used to respond to every request.
     * @return The {@link HttpHost} of the server
     * @throws IOException          in case it's not possible to start the server
     * @throws InterruptedException in case the server's main thread was interrupted
     */
    public HttpHost startServerWithGlobalRequestHandler(HttpRequestHandler handler)
            throws IOException, InterruptedException {
        // If there is an existing instance, terminate it
        tearDown();

        ServerBootstrap serverBootstrap = ServerBootstrap.bootstrap();

        serverBootstrap.registerHandler("/*", new BasicAsyncRequestHandler(handler));

        server = serverBootstrap.create();
        server.start();

        ListenerEndpoint endpoint = server.getEndpoint();
        endpoint.waitFor();

        InetSocketAddress address = (InetSocketAddress) endpoint.getAddress();
        return new HttpHost("localhost", address.getPort(), "http");
    }

    @After
    public void tearDown() {
        if (server != null) {
            server.shutdown(5, TimeUnit.SECONDS);
        }
    }
}
