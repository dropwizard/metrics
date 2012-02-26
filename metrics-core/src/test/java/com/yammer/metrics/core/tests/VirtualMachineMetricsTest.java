package com.yammer.metrics.core.tests;

import com.sun.management.UnixOperatingSystemMXBean;
import com.yammer.metrics.core.VMMFactory;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.core.VirtualMachineMetrics.GarbageCollectorStats;
import org.junit.Before;
import org.junit.Test;

import javax.management.*;
import java.lang.management.*;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class VirtualMachineMetricsTest {
    private final MemoryUsage heap = new MemoryUsage(1, 10, 100, 1000);
    private final MemoryUsage nonHeap = new MemoryUsage(2, 20, 200, 2000);
    private final MemoryMXBean memory = mock(MemoryMXBean.class);
    private final MemoryPoolMXBean pool1 = mock(MemoryPoolMXBean.class);
    private final MemoryPoolMXBean pool2 = mock(MemoryPoolMXBean.class);
    private final MemoryUsage pool1Usage = heap;
    private final MemoryUsage pool2Usage = nonHeap;
    private final List<MemoryPoolMXBean> memoryPools = asList(pool1,
                                                              pool2);
    private final UnixOperatingSystemMXBean os = mock(UnixOperatingSystemMXBean.class);
    private final ThreadMXBean threads = mock(ThreadMXBean.class);
    private final GarbageCollectorMXBean gc1 = mock(GarbageCollectorMXBean.class);
    private final GarbageCollectorMXBean gc2 = mock(GarbageCollectorMXBean.class);
    private final List<GarbageCollectorMXBean> garbageCollectors = asList(gc1, gc2);
    private final RuntimeMXBean runtime = mock(RuntimeMXBean.class);
    private final MBeanServer mBeanServer = mock(MBeanServer.class);

    private final VirtualMachineMetrics vmm = VMMFactory.build(memory,
                                                               memoryPools,
                                                               os,
                                                               threads,
                                                               garbageCollectors,
                                                               runtime,
                                                               mBeanServer);

    @Before
    public void setUp() throws Exception {
        when(memory.getHeapMemoryUsage()).thenReturn(heap);
        when(memory.getNonHeapMemoryUsage()).thenReturn(nonHeap);

        when(pool1.getUsage()).thenReturn(pool1Usage);
        when(pool1.getName()).thenReturn("pool1");

        when(pool2.getUsage()).thenReturn(pool2Usage);
        when(pool2.getName()).thenReturn("pool2");

        when(os.getOpenFileDescriptorCount()).thenReturn(50L);
        when(os.getMaxFileDescriptorCount()).thenReturn(1000L);

        when(runtime.getUptime()).thenReturn(11000L);

        when(threads.getThreadCount()).thenReturn(52);
        when(threads.getDaemonThreadCount()).thenReturn(22);

        when(gc1.getName()).thenReturn("gc1");
        when(gc1.getCollectionCount()).thenReturn(1L);
        when(gc1.getCollectionTime()).thenReturn(10L);

        when(gc2.getName()).thenReturn("gc2");
        when(gc2.getCollectionCount()).thenReturn(2L);
        when(gc2.getCollectionTime()).thenReturn(20L);
    }

    @Test
    public void calculatesTotalInit() throws Exception {
        assertThat(vmm.totalInit(),
                   is(3.0));
    }

    @Test
    public void calculatesTotalUsed() throws Exception {
        assertThat(vmm.totalUsed(),
                   is(30.0));
    }

    @Test
    public void calculatesTotalMax() throws Exception {
        assertThat(vmm.totalMax(),
                   is(3000.0));
    }

    @Test
    public void calculatesTotalCommitted() throws Exception {
        assertThat(vmm.totalCommitted(),
                   is(300.0));
    }

    @Test
    public void calculatesHeapInit() throws Exception {
        assertThat(vmm.heapInit(),
                   is(1.0));
    }

    @Test
    public void calculatesHeapUsed() throws Exception {
        assertThat(vmm.heapUsed(),
                   is(10.0));
    }

    @Test
    public void calculatesHeapCommitted() throws Exception {
        assertThat(vmm.heapCommitted(),
                   is(100.0));
    }

    @Test
    public void calculatesHeapMax() throws Exception {
        assertThat(vmm.heapMax(),
                   is(1000.0));
    }

    @Test
    public void calculatesNonHeapUsage() throws Exception {
        assertThat(vmm.nonHeapUsage(),
                   is(0.01));
    }

    @Test
    public void calculatesMemoryPoolUsage() throws Exception {
        final Map<String,Double> usages = vmm.memoryPoolUsage();

        assertThat(usages,
                   hasEntry("pool1", 0.01));

        assertThat(usages,
                   hasEntry("pool2", 0.01));
    }

    @Test
    public void calculatesFileDescriptorUsage() throws Exception {
        assertThat(vmm.fileDescriptorUsage(),
                   is(0.05));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fdCalculationHandlesNonUnixSystems() throws Exception {
        when(os.getOpenFileDescriptorCount()).thenThrow(NoSuchMethodException.class);

        assertThat(vmm.fileDescriptorUsage(),
                   is(Double.NaN));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fdCalculationHandlesSecuredSystems() throws Exception {
        when(os.getOpenFileDescriptorCount()).thenThrow(IllegalAccessException.class);

        assertThat(vmm.fileDescriptorUsage(),
                   is(Double.NaN));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void fdCalculationHandlesWeirdSystems() throws Exception {
        when(os.getOpenFileDescriptorCount()).thenThrow(InvocationTargetException.class);

        assertThat(vmm.fileDescriptorUsage(),
                   is(Double.NaN));
    }

    @Test
    public void fetchesTheVMName() throws Exception {
        // this is ugly, but I'd rather not dick with the system properties
        assertThat(vmm.name(),
                   is(System.getProperty("java.vm.name")));
    }

    @Test
    public void calculatesTheUptimeInSeconds() throws Exception {
        assertThat(vmm.uptime(),
                   is(11L));
    }

    @Test
    public void calculatesTheThreadCount() throws Exception {
        assertThat(vmm.threadCount(),
                   is(52));
    }

    @Test
    public void calculatesTheDaemonThreadCount() throws Exception {
        assertThat(vmm.daemonThreadCount(),
                   is(22));
    }

    @Test
    public void calculatesGcStats() throws Exception {
        final Map<String, GarbageCollectorStats> stats = vmm.garbageCollectors();

        assertThat(stats.get("gc1").getRuns(),
                   is(1L));

        assertThat(stats.get("gc1").getTime(TimeUnit.MILLISECONDS),
                   is(10L));

        assertThat(stats.get("gc2").getRuns(),
                   is(2L));

        assertThat(stats.get("gc2").getTime(TimeUnit.MILLISECONDS),
                   is(20L));
    }

    @Test
    public void handlesMissingBufferPools() throws Exception {
        when(mBeanServer.getAttributes(any(ObjectName.class), any(String[].class))).thenThrow(new InstanceNotFoundException("OH NO"));

        assertThat(vmm.getBufferPoolStats().isEmpty(),
                   is(true));
    }

    @Test
    public void handlesMappedAndDirectBufferPools() throws Exception {
        final String[] attributes = { "Count", "MemoryUsed", "TotalCapacity" };

        final ObjectName direct = new ObjectName("java.nio:type=BufferPool,name=direct");
        final ObjectName mapped = new ObjectName("java.nio:type=BufferPool,name=mapped");

        final AttributeList directAttributes = new AttributeList();
        directAttributes.add(new Attribute("Count", 100L));
        directAttributes.add(new Attribute("MemoryUsed", 200L));
        directAttributes.add(new Attribute("TotalCapacity", 300L));

        final AttributeList mappedAttributes = new AttributeList();
        mappedAttributes.add(new Attribute("Count", 1000L));
        mappedAttributes.add(new Attribute("MemoryUsed", 2000L));
        mappedAttributes.add(new Attribute("TotalCapacity", 3000L));

        when(mBeanServer.getAttributes(direct, attributes)).thenReturn(directAttributes);
        when(mBeanServer.getAttributes(mapped, attributes)).thenReturn(mappedAttributes);

        assertThat(vmm.getBufferPoolStats().get("direct").getCount(),
                   is(100L));

        assertThat(vmm.getBufferPoolStats().get("direct").getMemoryUsed(),
                   is(200L));

        assertThat(vmm.getBufferPoolStats().get("direct").getTotalCapacity(),
                   is(300L));

        assertThat(vmm.getBufferPoolStats().get("mapped").getCount(),
                   is(1000L));

        assertThat(vmm.getBufferPoolStats().get("mapped").getMemoryUsed(),
                   is(2000L));

        assertThat(vmm.getBufferPoolStats().get("mapped").getTotalCapacity(),
                   is(3000L));
    }

    // TODO: 1/13/12 <coda> -- test thread state percentages
    // TODO: 1/13/12 <coda> -- test thread dumps
}
