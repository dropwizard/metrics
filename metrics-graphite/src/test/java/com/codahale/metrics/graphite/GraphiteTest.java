package com.codahale.metrics.graphite;

import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class GraphiteTest {
    private final String host = "example.com";
    private final int port = 1234;
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress(host, port);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = spy(new ByteArrayOutputStream());

    private Graphite graphite;

    @Before
    public void setUp() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean(true);
        final AtomicBoolean closed = new AtomicBoolean(false);

        when(socket.isConnected()).thenAnswer(invocation -> connected.get());

        when(socket.isClosed()).thenAnswer(invocation -> closed.get());

        doAnswer(invocation -> {
            connected.set(false);
            closed.set(true);
            return null;
        }).when(socket).close();

        when(socket.getOutputStream()).thenReturn(output);

        // Mock behavior of socket.getOutputStream().close() calling socket.close();
        doAnswer(invocation -> {
            invocation.callRealMethod();
            socket.close();
            return null;
        }).when(output).close();

        when(socketFactory.createSocket(any(InetAddress.class), anyInt())).thenReturn(socket);
    }

    @Test
    public void connectsToGraphiteWithInetSocketAddress() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();

        verify(socketFactory).createSocket(address.getAddress(), address.getPort());
    }

    @Test
    public void connectsToGraphiteWithHostAndPort() throws Exception {
        graphite = new Graphite(host, port, socketFactory);
        graphite.connect();

        verify(socketFactory).createSocket(address.getAddress(), port);
    }

    @Test
    public void measuresFailures() throws Exception {
        graphite = new Graphite(address, socketFactory);
        assertThat(graphite.getFailures())
                .isZero();
    }

    @Test
    public void disconnectsFromGraphite() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();
        graphite.close();

        verify(socket, times(2)).close();
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();
        try {
            graphite.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Already connected");
        }
    }

    @Test
    public void writesValuesToGraphite() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();
        graphite.send("name", "value", 100);
        graphite.close();

        assertThat(output.toString())
                .isEqualTo("name value 100\n");
    }

    @Test
    public void sanitizesNames() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();
        graphite.send("name woo", "value", 100);
        graphite.close();

        assertThat(output.toString())
                .isEqualTo("name-woo value 100\n");
    }

    @Test
    public void sanitizesValues() throws Exception {
        graphite = new Graphite(address, socketFactory);
        graphite.connect();
        graphite.send("name", "value woo", 100);
        graphite.close();

        assertThat(output.toString())
                .isEqualTo("name value-woo 100\n");
    }

    @Test
    public void notifiesIfGraphiteIsUnavailable() throws Exception {
        final String unavailableHost = "unknown-host-10el6m7yg56ge7dmcom";
        InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);
        Graphite unavailableGraphite = new Graphite(unavailableAddress, socketFactory);

        try {
            unavailableGraphite.connect();
            failBecauseExceptionWasNotThrown(UnknownHostException.class);
        } catch (Exception e) {
            assertThat(e.getMessage())
                .isEqualTo(unavailableHost);
        }
    }
}
