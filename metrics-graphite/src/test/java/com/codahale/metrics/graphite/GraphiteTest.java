package com.codahale.metrics.graphite;

import org.junit.Before;
import org.junit.Test;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
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
    private final ByteArrayOutputStream output = spy(ByteArrayOutputStream.class);

    @Before
    public void setUp() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean(true);
        final AtomicBoolean closed = new AtomicBoolean(false);

        when(socket.isConnected()).thenAnswer(invocation -> connected.get());

        when(socket.isClosed()).thenAnswer(invocation -> closed.get());

        doNothing().when(socket).connect(any(SocketAddress.class), anyInt());
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

        when(socketFactory.createSocket()).thenReturn(socket);
    }

    @Test
    public void connectsToGraphiteWithInetSocketAddress() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
        }
        verify(socketFactory).createSocket();
        verify(socket).connect(eq(address), anyInt());
    }

    @Test
    public void connectsToGraphiteWithHostAndPort() throws Exception {
        ArgumentCaptor <InetSocketAddress> addressCaptor = ArgumentCaptor.forClass(InetSocketAddress.class);
        doNothing().when(socket).connect(addressCaptor.capture(), anyInt());

        try (Graphite graphite = new Graphite(host, port, socketFactory)) {
            graphite.connect();
        }
        verify(socketFactory).createSocket();
        verify(socket).connect(any(InetSocketAddress.class), anyInt());
        
        assertThat(addressCaptor.getValue().getHostString()).isEqualTo(host);
        assertThat(addressCaptor.getValue().getPort()).isEqualTo(port);

    }

    @Test
    public void measuresFailures() throws IOException {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            assertThat(graphite.getFailures()).isZero();
        }
    }

    @Test
    public void disconnectsFromGraphite() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
        }

        verify(socket, times(2)).close();
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            assertThatNoException().isThrownBy(graphite::connect);
            assertThatThrownBy(graphite::connect)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("Already connected");
        }
    }

    @Test
    public void writesValuesToGraphite() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name", "value", 100);
        }
        assertThat(output).hasToString("name value 100\n");
    }

    @Test
    public void sanitizesNames() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name woo", "value", 100);
        }
        assertThat(output).hasToString("name-woo value 100\n");
    }

    @Test
    public void sanitizesValues() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name", "value woo", 100);
        }
        assertThat(output).hasToString("name value-woo 100\n");
    }

    @Test
    public void notifiesIfGraphiteIsUnavailable() throws IOException {
        final String unavailableHost = "unknown-host-10el6m7yg56ge7dmcom";
        InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);

        try (Graphite unavailableGraphite = new Graphite(unavailableAddress, socketFactory)) {
            assertThatThrownBy(unavailableGraphite::connect)
                    .isInstanceOf(UnknownHostException.class)
                    .hasMessage(unavailableHost);
        }
    }
}
