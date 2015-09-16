package io.dropwizard.metrics.atsd;

import io.dropwizard.metrics.MetricName;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.net.SocketFactory;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class AtsdTCPSenderTest {
    private final String host = "example.com";
    private final int port = 1234;
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress(host, port);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = spy(new ByteArrayOutputStream());

    private AtsdTCPSender sender;

    @Before
    public void setUp() throws Exception {
        final AtomicBoolean connected = new AtomicBoolean(true);
        final AtomicBoolean closed = new AtomicBoolean(false);

        when(socket.isConnected()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return connected.get();
            }
        });

        when(socket.isClosed()).thenAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                return closed.get();
            }
        });

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                connected.set(false);
                closed.set(true);
                return null;
            }
        }).when(socket).close();

        when(socket.getOutputStream()).thenReturn(output);

        // Mock behavior of socket.getOutputStream().close() calling socket.close();
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                invocation.callRealMethod();
                socket.close();
                return null;
            }
        }).when(output).close();

        when(socketFactory.createSocket(any(InetAddress.class), anyInt())).thenReturn(socket);
    }

    @Test
    public void connectsToATSDWithInetSocketAddress() throws Exception {
        sender = new AtsdTCPSender(address, socketFactory);
        sender.connect();

        verify(socketFactory).createSocket(address.getAddress(), address.getPort());
    }

    @Test
    public void connectsToATSDWithHostAndPort() throws Exception {
        sender = new AtsdTCPSender(host, port, socketFactory);
        sender.connect();

        verify(socketFactory).createSocket(address.getAddress(), port);
    }

    @Test
    public void measuresFailures() throws Exception {
        sender = new AtsdTCPSender(address, socketFactory);
        assertThat(sender.getFailures())
                .isZero();
    }

    @Test
    public void disconnectsFromATSD() throws Exception {
        sender = new AtsdTCPSender(address, socketFactory);
        sender.connect();
        sender.close();

        verify(socket).close();
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        sender = new AtsdTCPSender(address, socketFactory);
        sender.connect();
        try {
            sender.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Already connected");
        }
    }

    @Test
    public void writesValuesToATSD() throws Exception {
        HashMap<String, String> tags = new HashMap<String, String>();
        tags.put("tagKey", "tagValue");
        MetricName metric = new MetricName("metricName", tags);

        sender = new AtsdTCPSender(address, socketFactory);
        sender.connect();
        sender.send("entityName", metric, "100", 100);
        sender.close();

        assertThat(output.toString())
                .isEqualTo("series e:entityName m:metricName=100 t:tagKey=tagValue ms:100\n");
    }

    @Test
    public void notifiesIfATSDIsUnavailable() throws Exception {
        final String unavailableHost = "unknown-host-10el6m7yg56ge7dm.com";
        InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);
        AtsdTCPSender unavailableATSD = new AtsdTCPSender(unavailableAddress, socketFactory);

        try {
            unavailableATSD.connect();
            failBecauseExceptionWasNotThrown(UnknownHostException.class);
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .isEqualTo(unavailableHost);
        }
    }
}
