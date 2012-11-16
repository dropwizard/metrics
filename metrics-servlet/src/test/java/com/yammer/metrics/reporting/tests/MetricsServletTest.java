package com.yammer.metrics.reporting.tests;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.MetricsServlet;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsServletTest {
    private final Clock clock = mock(Clock.class);
    private final VirtualMachineMetrics vm = mock(VirtualMachineMetrics.class);
    private final MetricsRegistry registry = new MetricsRegistry(clock);
    private final JsonFactory factory = mock(JsonFactory.class);

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final ServletOutputStream output = mock(ServletOutputStream.class);
    private final MetricsServlet servlet = new MetricsServlet(clock, vm, registry, factory, false);

    private final ByteArrayOutputStream json = new ByteArrayOutputStream();

    @Before
    public void setUp() throws Exception {
        when(clock.time()).thenReturn(12345678L);

        when(request.getMethod()).thenReturn("GET");

        when(response.getOutputStream()).thenReturn(output);

        final JsonGenerator generator = new JsonFactory(new ObjectMapper()).createJsonGenerator(json,
                                                                                                JsonEncoding.UTF8);
        when(factory.createJsonGenerator(output, JsonEncoding.UTF8)).thenReturn(generator);
    }

    @Test
    public void generatesVirtualMachineMetrics() throws Exception {
        when(vm.name()).thenReturn("vm");
        when(vm.version()).thenReturn("version");
        when(vm.totalInit()).thenReturn(1.0);
        when(vm.totalUsed()).thenReturn(2.0);
        when(vm.totalMax()).thenReturn(3.0);
        when(vm.totalCommitted()).thenReturn(4.0);
        when(vm.heapInit()).thenReturn(5.0);
        when(vm.heapUsed()).thenReturn(6.0);
        when(vm.heapMax()).thenReturn(7.0);
        when(vm.heapCommitted()).thenReturn(8.0);

        final Map<String, Double> pools = new TreeMap<String, Double>();
        pools.put("one", 100.0);
        pools.put("two", 200.0);
        when(vm.memoryPoolUsage()).thenReturn(pools);

        when(vm.daemonThreadCount()).thenReturn(300);
        when(vm.threadCount()).thenReturn(400);

        when(vm.heapUsage()).thenReturn(34.0);
        when(vm.nonHeapUsage()).thenReturn(37.0);
        when(vm.uptime()).thenReturn(9991L);
        when(vm.fileDescriptorUsage()).thenReturn(0.222);

        final Map<Thread.State, Double> threads = new TreeMap<Thread.State, Double>();
        threads.put(Thread.State.BLOCKED, 0.33);
        when(vm.threadStatePercentages()).thenReturn(threads);

        final Map<String, VirtualMachineMetrics.GarbageCollectorStats> gcs =
                new TreeMap<String, VirtualMachineMetrics.GarbageCollectorStats>();

        final VirtualMachineMetrics.GarbageCollectorStats gc = mock(VirtualMachineMetrics.GarbageCollectorStats.class);
        when(gc.getTime(TimeUnit.MILLISECONDS)).thenReturn(40L);
        when(gc.getRuns()).thenReturn(20L);
        gcs.put("one", gc);
        when(vm.garbageCollectors()).thenReturn(gcs);

        final VirtualMachineMetrics.BufferPoolStats direct = mock(VirtualMachineMetrics.BufferPoolStats.class);
        when(direct.getCount()).thenReturn(1L);
        when(direct.getMemoryUsed()).thenReturn(2L);
        when(direct.getTotalCapacity()).thenReturn(3L);
        
        final VirtualMachineMetrics.BufferPoolStats mapped = mock(VirtualMachineMetrics.BufferPoolStats.class);
        when(mapped.getCount()).thenReturn(10L);
        when(mapped.getMemoryUsed()).thenReturn(20L);
        when(mapped.getTotalCapacity()).thenReturn(30L);

        final Map<String, VirtualMachineMetrics.BufferPoolStats> bufferPoolStats =
                new TreeMap<String, VirtualMachineMetrics.BufferPoolStats>();

        bufferPoolStats.put("direct", direct);
        bufferPoolStats.put("mapped", mapped);

        when(vm.getBufferPoolStats()).thenReturn(bufferPoolStats);

        final MetricsServlet servlet = new MetricsServlet(clock, vm, registry, factory, true);

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"jvm\":{\"vm\":{\"name\":\"vm\",\"version\":\"version\"},\"memory\":{" +
                              "\"totalInit\":1.0,\"totalUsed\":2.0,\"totalMax\":3.0," +
                              "\"totalCommitted\":4.0,\"heapInit\":5.0,\"heapUsed\":6.0,\"" +
                              "heapMax\":7.0,\"heapCommitted\":8.0,\"heap_usage\":34.0," +
                              "\"non_heap_usage\":37.0,\"memory_pool_usages\":{\"one\":100.0," +
                              "\"two\":200.0}},\"buffers\":{\"direct\":{\"count\":1," +
                              "\"memoryUsed\":2,\"totalCapacity\":3},\"mapped\":{\"count\":10," +
                              "\"memoryUsed\":20,\"totalCapacity\":30}},\"daemon_thread_count\":300," +
                              "\"thread_count\":400,\"current_time\":12345678,\"uptime\":9991," +
                              "\"fd_usage\":0.222,\"thread-states\":{\"blocked\":0.33}," +
                              "\"garbage-collectors\":{\"one\":{\"runs\":20,\"time\":40}}}}"));
    }

    @Test
    public void generatesGauges() throws Exception {
        registry.newGauge(MetricsServletTest.class, "gauge", new Gauge<Double>() {
            @Override
            public Double value() {
                return 22.2;
            }
        });

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"com.yammer.metrics.reporting.tests.MetricsServletTest\":" +
                              "{\"gauge\":{\"type\":\"gauge\",\"value\":22.2}}}"));
    }

    @Test
    public void generatesCounters() throws Exception {
        registry.newCounter(MetricsServletTest.class, "counter").inc(12);

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"com.yammer.metrics.reporting.tests.MetricsServletTest\":" +
                              "{\"counter\":{\"type\":\"counter\",\"count\":12}}}"));
    }

    @Test
    public void generatesHistograms() throws Exception {
        registry.newHistogram(MetricsServletTest.class, "histogram").update(12);

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"com.yammer.metrics.reporting.tests.MetricsServletTest\":" +
                              "{\"histogram\":{\"type\":\"histogram\",\"count\":1,\"min\":12.0," +
                              "\"max\":12.0,\"mean\":12.0,\"std_dev\":0.0,\"median\":12.0," +
                              "\"p75\":12.0,\"p95\":12.0,\"p98\":12.0,\"p99\":12.0,\"p999\":12.0}}}"));
    }

    @Test
    public void generatesMeters() throws Exception {
        when(clock.tick()).thenReturn(100000L, 110000L);

        registry.newMeter(MetricsServletTest.class, "meter", "things", TimeUnit.SECONDS)
                .mark(12);

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"com.yammer.metrics.reporting.tests.MetricsServletTest\":" +
                              "{\"meter\":{\"type\":\"meter\",\"event_type\":\"things\"," +
                              "\"unit\":\"seconds\",\"count\":12,\"mean\":1200000.0," +
                              "\"m1\":0.0,\"m5\":0.0,\"m15\":0.0}}}"));
    }

    @Test
    public void generatesTimers() throws Exception {
        when(clock.tick()).thenReturn(100000L, 110000L);

        registry.newTimer(MetricsServletTest.class, "timer").update(100, TimeUnit.MILLISECONDS);

        servlet.service(request, response);

        assertThat(json.toString(),
                   is("{\"com.yammer.metrics.reporting.tests.MetricsServletTest\":{\"timer\":" +
                              "{\"type\":\"timer\",\"duration\":{\"unit\":\"milliseconds\"," +
                              "\"min\":100.0,\"max\":100.0,\"mean\":100.0,\"std_dev\":0.0," +
                              "\"median\":100.0,\"p75\":100.0,\"p95\":100.0,\"p98\":100.0," +
                              "\"p99\":100.0,\"p999\":100.0},\"rate\":{\"unit\":\"seconds\"," +
                              "\"count\":1,\"mean\":100000.0,\"m1\":0.0,\"m5\":0.0," +
                              "\"m15\":0.0}}}}"));
    }

    // TODO: 1/19/12 <coda> -- test class prefix
    // TODO: 1/19/12 <coda> -- test pretty printing
    // TODO: 1/19/12 <coda> -- test full sample dumping
    // TODO: 1/19/12 <coda> -- test servlet configuring
}
