package com.codahale.metrics.graphite;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.python.core.PyList;
import org.python.core.PyTuple;

import javax.net.SocketFactory;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;

public class PickledGraphiteTest {
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress("example.com", 1234);
    private final PickledGraphite graphite = new PickledGraphite(address, socketFactory, UTF_8, 2);

    private final Socket socket = mock(Socket.class);
    private final ByteArrayOutputStream output = spy(new ByteArrayOutputStream());

    // Pulls apart the pickled payload. This skips ahead 4 characters to safely ignore
    // the header (length)
    private static final String UNPICKLER_SCRIPT =
        "import cPickle\n" +
            "import struct\n" +
            "format = '!L'\n" +
            "headerLength = struct.calcsize(format)\n" +
            "payloadLength, = struct.unpack(format, payload[:headerLength])\n" +
            "batchLength = headerLength + payloadLength.intValue()\n" +
            "metrics = cPickle.loads(payload[headerLength:batchLength])\n";

    private CompiledScript unpickleScript;

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

        when(socketFactory.createSocket(any(InetAddress.class),
            anyInt())).thenReturn(socket);

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");
        Compilable compilable = (Compilable) engine;
        unpickleScript = compilable.compile(UNPICKLER_SCRIPT);
    }

    @Test
    public void disconnectsFromGraphite() throws Exception {
        graphite.connect();
        graphite.close();

        verify(socket).close();
    }

    @Test
    public void writesValuesToGraphite() throws Exception {
        graphite.connect();
        graphite.send("name", "value", 100);
        graphite.close();

        assertThat(unpickleOutput())
            .isEqualTo("name value 100\n");
    }

    @Test
    public void writesFullBatch() throws Exception {
        graphite.connect();
        graphite.send("name", "value", 100);
        graphite.send("name", "value2", 100);
        graphite.close();

        assertThat(unpickleOutput())
            .isEqualTo("name value 100\nname value2 100\n");
    }

    @Test
    public void writesPastFullBatch() throws Exception {
        graphite.connect();
        graphite.send("name", "value", 100);
        graphite.send("name", "value2", 100);
        graphite.send("name", "value3", 100);
        graphite.close();

        assertThat(unpickleOutput())
            .isEqualTo("name value 100\nname value2 100\nname value3 100\n");
    }

    @Test
    public void sanitizesNames() throws Exception {
        graphite.connect();
        graphite.send("name woo", "value", 100);
        graphite.close();

        assertThat(unpickleOutput())
            .isEqualTo("name-woo value 100\n");
    }

    @Test
    public void sanitizesValues() throws Exception {
        graphite.connect();
        graphite.send("name", "value woo", 100);
        graphite.close();

        assertThat(unpickleOutput())
            .isEqualTo("name value-woo 100\n");
    }

    @Test
    public void doesNotAllowDoubleConnections() throws Exception {
        graphite.connect();
        try {
            graphite.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage())
                    .isEqualTo("Already connected");
        }
    }

    private String unpickleOutput() throws Exception {
        StringBuilder results = new StringBuilder();

        // the charset is important. if the GraphitePickleReporter and this test
        // don't agree, the header is not always correctly unpacked.
        String payload = output.toString("UTF-8");

        PyList result = new PyList();
        int nextIndex = 0;
        while (nextIndex < payload.length()) {
            Bindings bindings = new SimpleBindings();
            bindings.put("payload", payload.substring(nextIndex));
            unpickleScript.eval(bindings);
            result.addAll(result.size(), (PyList) bindings.get("metrics"));
            nextIndex += (Integer) bindings.get("batchLength");
        }

        for (Object aResult : result) {
            PyTuple datapoint = (PyTuple) aResult;
            String name = datapoint.get(0).toString();
            PyTuple valueTuple = (PyTuple) datapoint.get(1);
            Object timestamp = valueTuple.get(0);
            Object value = valueTuple.get(1);

            results.append(name).append(" ").append(value).append(" ").append(timestamp).append("\n");
        }

        return results.toString();
    }
}
