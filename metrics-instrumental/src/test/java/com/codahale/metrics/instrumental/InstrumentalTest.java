package com.codahale.metrics.instrumental;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InstrumentalTest {
    private final String host = "example.com";
    private final int port = 1234;
    private final String apiKey = "Th3Ap1K3y";
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress(host, port);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = spy(new ByteArrayOutputStream());

    private Instrumental instrumental;

    @Before
    public void setUp() throws Exception {
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

        doAnswer(new Answer<InetAddress>() {
            @Override
            public InetAddress answer(InvocationOnMock invocation) throws Throwable {
                return InetAddress.getLocalHost();
            }
        }).when(socket).getLocalAddress();

        when(socketFactory.createSocket()).thenReturn(socket);
    }

    @Test
    public void connectsToInstrumentalWithInetSocketAddress() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();

        verify(socketFactory).createSocket();
        verify(socket).setTcpNoDelay(true);
        verify(socket).setKeepAlive(true);
        verify(socket).setTrafficClass(0x04 | 0x10);
        verify(socket).setPerformancePreferences(0, 2, 1);
        verify(socket).connect(address);
    }

    @Test
    public void connectsToInstrumentalWithHostAndPort() throws Exception {
        instrumental = new Instrumental(apiKey, host, port, socketFactory);
        instrumental.connect();

        verify(socketFactory).createSocket();
        verify(socket).setTcpNoDelay(true);
        verify(socket).setKeepAlive(true);
        verify(socket).setTrafficClass(0x04 | 0x10);
        verify(socket).setPerformancePreferences(0, 2, 1);
        verify(socket).connect(address);
    }

    @Test
    public void measuresFailures() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        assertThat(instrumental.getFailures())
                .isZero();
    }

    @Test
    public void disconnectsFromInstrumental() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        instrumental.close();

        verify(socket).close();
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        try {
            instrumental.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Already connected");
        }
    }

    @Test
    public void handshakesProperly() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        instrumental.close();

        assertThat(output.toString()).matches("hello version .* hostname .* pid .* runtime .* platform .*\\n.*\\n");
        assertThat(output.toString()).contains("authenticate " + apiKey);
    }


    @Test
    public void writesValuesToInstrumental() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        output.reset();
        instrumental.send("name", "value", 100);
        instrumental.close();

        assertThat(output.toString())
                .isEqualTo("gauge name value 100\n");
    }

    @Test
    public void sanitizesNames() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        output.reset();
        instrumental.send("name woo", "value", 100);
        instrumental.close();

        assertThat(output.toString())
                .isEqualTo("gauge name.woo value 100\n");
    }

    @Test
    public void sanitizesValues() throws Exception {
        instrumental = new Instrumental(apiKey, address, socketFactory);
        instrumental.connect();
        output.reset();
        instrumental.send("name", "value woo", 100);
        instrumental.close();

        assertThat(output.toString())
                .isEqualTo("gauge name value.woo 100\n");
    }

    @Test
    public void notifiesIfInstrumentalIsUnavailable() throws Exception {
        final String unavailableHost = "unknown-host-10el6m7yg56ge7dm.com";
        InetSocketAddress unavailableAddress = new InetSocketAddress(unavailableHost, 1234);
        Instrumental unavailableInstrumental = new Instrumental(apiKey, unavailableAddress, socketFactory);

        try {
            unavailableInstrumental.connect();
            failBecauseExceptionWasNotThrown(UnknownHostException.class);
        } catch (Exception e) {
            assertThat(e.getMessage())
                .isEqualTo(unavailableHost);
        }
    }
}
