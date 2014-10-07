package com.codahale.metrics.graphite;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
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
import java.nio.charset.Charset;

public class PickledGraphiteTest {
    private final SocketFactory socketFactory = mock(SocketFactory.class);
    private final InetSocketAddress address = new InetSocketAddress("example.com", 1234);
    private final PickledGraphite graphite = new PickledGraphite(address, socketFactory, Charset.forName("UTF-8"), 2);

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

    String unpickleOutput() throws Exception {
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
