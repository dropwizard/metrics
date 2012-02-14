package com.yammer.metrics.reporting;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

import org.junit.Before;
import org.python.core.PyList;
import org.python.core.PyTuple;

import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Metric;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

public class GraphitePickleReporterTest extends AbstractPollingReporterTest {
    
    // Pulls apart the pickled paylost. This skips ahead 4 characters to safely ignore
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
    public void before() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("python");            
        Compilable compilable = (Compilable) engine;
        unpickleScript = compilable.compile(UNPICKLER_SCRIPT);
    }
    
    @Override
    protected AbstractPollingReporter createReporter(MetricsRegistry registry, OutputStream out, Clock clock) throws Exception {
        final Socket socket = mock(Socket.class);
        when(socket.getOutputStream()).thenReturn(out);

        final SocketProvider provider = mock(SocketProvider.class);
        when(provider.get()).thenReturn(socket);

        // use a small batch size so the boundary cases are tested
        final GraphitePickleReporter reporter = new GraphitePickleReporter(registry,
                                                               "prefix",
                                                               MetricPredicate.ALL,
                                                               provider,
                                                               clock,
                                                               2);
        reporter.printVMMetrics = false;
        return reporter;
    }

    @Override
    public String[] expectedGaugeResult(String value) {
        return new String[]{String.format("prefix.java.lang.Object.metric.value %s 5", value)};
    }

    @Override
    public String[] expectedTimerResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
                "prefix.java.lang.Object.metric.min 1.00 5",
                "prefix.java.lang.Object.metric.max 3.00 5",
                "prefix.java.lang.Object.metric.mean 2.00 5",
                "prefix.java.lang.Object.metric.stddev 1.50 5",
                "prefix.java.lang.Object.metric.median 0.50 5",
                "prefix.java.lang.Object.metric.75percentile 0.75 5",
                "prefix.java.lang.Object.metric.95percentile 0.95 5",
                "prefix.java.lang.Object.metric.98percentile 0.98 5",
                "prefix.java.lang.Object.metric.99percentile 0.99 5",
                "prefix.java.lang.Object.metric.999percentile 1.00 5"
        };
    }

    @Override
    public String[] expectedMeterResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.count 1 5",
                "prefix.java.lang.Object.metric.meanRate 2.00 5",
                "prefix.java.lang.Object.metric.1MinuteRate 1.00 5",
                "prefix.java.lang.Object.metric.5MinuteRate 5.00 5",
                "prefix.java.lang.Object.metric.15MinuteRate 15.00 5",
        };
    }

    @Override
    public String[] expectedHistogramResult() {
        return new String[]{
                "prefix.java.lang.Object.metric.min 1.00 5",
                "prefix.java.lang.Object.metric.max 3.00 5",
                "prefix.java.lang.Object.metric.mean 2.00 5",
                "prefix.java.lang.Object.metric.stddev 1.50 5",
                "prefix.java.lang.Object.metric.median 0.50 5",
                "prefix.java.lang.Object.metric.75percentile 0.75 5",
                "prefix.java.lang.Object.metric.95percentile 0.95 5",
                "prefix.java.lang.Object.metric.98percentile 0.98 5",
                "prefix.java.lang.Object.metric.99percentile 0.99 5",
                "prefix.java.lang.Object.metric.999percentile 1.00 5"
        };
    }

    @Override
    public String[] expectedCounterResult(long count) {
        return new String[]{
                String.format("prefix.java.lang.Object.metric.count %d 5", count)
        };
    }

    protected <T extends Metric> void assertReporterOutput(Callable<T> action, String... expected) throws Exception {
        // Invoke the callable to trigger (ie, mark()/inc()/etc) and return the metric
        final T metric = action.call();
        try {
            // Add the metric to the registry, run the reporter and flush the result
            registry.add(new MetricName(Object.class, "metric"), metric);
            reporter.run();
            out.flush();
            // the charset is important. if the GraphitePickleReporter and this test
            // don't agree, the header is not always correctly unpacked.
            String payload = out.toString("UTF-8");
            
            PyList result = new PyList();

            int nextIndex = 0;
            while(nextIndex < payload.length()) {
                Bindings bindings = new SimpleBindings();
                bindings.put("payload", payload.substring(nextIndex));
                unpickleScript.eval(bindings);
                result.addAll(result.size(), (PyList) bindings.get("metrics"));
                nextIndex += (Integer) bindings.get("batchLength");
            }
            
            // Assertions: first check that the line count matches then compare line by line ignoring leading and trailing whitespace
            assertEquals("Line count mismatch, was:\n" + payload, expected.length,
                         result.size());
            for (int i = 0; i < result.size(); i++) {
                PyTuple datapoint = (PyTuple) result.get(i);
                String name = datapoint.get(0).toString();
                PyTuple valueTuple = (PyTuple) datapoint.get(1);
                Object timestamp = valueTuple.get(0);
                Object value = valueTuple.get(1);
                
                String actual = name + " " + value + " " + timestamp;
                if (!expected[i].trim().equals(actual.trim())) {
                    System.err.println("Failure comparing line " + (1 + i));
                    System.err.println("Was:      '" + actual + "'");
                    System.err.println("Expected: '" + expected[i] + "'\n");
                }
                assertEquals(expected[i].trim(), actual.trim());
            }
        } finally {
            reporter.shutdown();
        }
    }

}
