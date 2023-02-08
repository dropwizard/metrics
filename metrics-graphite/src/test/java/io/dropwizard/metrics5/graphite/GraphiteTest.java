package io.dropwizard.metrics5.graphite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GraphiteTest {
    private final String host = "example.com";
    private final int port = 1234;
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress(host, port);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = spy(ByteArrayOutputStream.class);

    @BeforeEach
    void setUp() throws Exception {
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
    void connectsToGraphiteWithInetSocketAddress() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
        }
        verify(socketFactory).createSocket(address.getAddress(), address.getPort());
    }

    @Test
    void connectsToGraphiteWithHostAndPort() throws Exception {
        try (Graphite graphite = new Graphite(host, port, socketFactory)) {
            graphite.connect();
        }
        verify(socketFactory).createSocket(address.getAddress(), port);
    }

    @Test
    void measuresFailures() throws IOException {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            assertThat(graphite.getFailures()).isZero();
        }
    }

    @Test
    void disconnectsFromGraphite() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
        }

        verify(socket, times(2)).close();
    }

    @Test
    void doesNotAllowDoubleConnections() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            assertThatNoException().isThrownBy(graphite::connect);
            assertThatThrownBy(graphite::connect)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Already connected");
        }
    }

    @Test
    void writesValuesToGraphite() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name", "value", 100);
        }
        assertThat(output).hasToString("name value 100\n");
    }

    @Test
    void sanitizesNames() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name woo", "value", 100);
        }
        assertThat(output).hasToString("name-woo value 100\n");
    }

    @Test
    void sanitizesValues() throws Exception {
        try (Graphite graphite = new Graphite(address, socketFactory)) {
            graphite.connect();
            graphite.send("name", "value woo", 100);
        }
        assertThat(output).hasToString("name value-woo 100\n");
    }

    @Test
    void notifiesIfGraphiteIsUnavailable() throws IOException {
        final String unavailableHost = "unknown-host-10el6m7yg56ge7dmcom";
        InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);

        try (Graphite unavailableGraphite = new Graphite(unavailableAddress, socketFactory)) {
            assertThatThrownBy(unavailableGraphite::connect)
                .isInstanceOf(UnknownHostException.class)
                .hasMessage(unavailableHost);
        }
    }
}
