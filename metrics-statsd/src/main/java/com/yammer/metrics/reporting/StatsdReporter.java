package com.yammer.metrics.reporting;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.Locale;

public class StatsdReporter extends AbstractPollingReporter implements MetricProcessor<Long> {
    private static final Logger LOG = LoggerFactory.getLogger(StatsdReporter.class);

    protected final String prefix;
    protected final MetricPredicate predicate;
    protected final Locale locale = Locale.US;
    protected final Clock clock;
    protected final SocketProvider socketProvider;
    protected final VirtualMachineMetrics vm;
    protected Writer writer;

    private boolean printVMMetrics = true;

    public interface SocketProvider {
        Socket get() throws Exception;
    }

    public StatsdReporter(String host, int port) throws IOException {
        this(Metrics.defaultRegistry(), host, port, null);
    }

    public StatsdReporter(String host, int port, String prefix) throws IOException {
        this(Metrics.defaultRegistry(), host, port, prefix);
    }

    public StatsdReporter(MetricsRegistry metricsRegistry, String host, int port) throws IOException {
        this(metricsRegistry, host, port, null);
    }

    public StatsdReporter(MetricsRegistry metricsRegistry, String host, int port, String prefix) throws IOException {
        this(metricsRegistry,
             prefix,
             MetricPredicate.ALL,
             new DefaultSocketProvider(host, port),
             Clock.defaultClock());
    }

    public StatsdReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock) throws IOException {
        this(metricsRegistry, prefix, predicate, socketProvider, clock, VirtualMachineMetrics.getInstance());
    }

    public StatsdReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock, VirtualMachineMetrics vm) throws IOException {
        this(metricsRegistry, prefix, predicate, socketProvider, clock, vm, "graphite-reporter");
    }

    public StatsdReporter(MetricsRegistry metricsRegistry, String prefix, MetricPredicate predicate, SocketProvider socketProvider, Clock clock, VirtualMachineMetrics vm, String name) throws IOException {
        super(metricsRegistry, name);

        this.socketProvider = socketProvider;
        this.vm = vm;

        this.clock = clock;

        if (prefix != null) {
            // Pre-append the "." so that we don't need to make anything conditional later.
            this.prefix = prefix + ".";
        } else {
            this.prefix = "";
        }
        this.predicate = predicate;
    }

    public boolean isPrintVMMetrics() {
        return printVMMetrics;
    }

    public void setPrintVMMetrics(boolean printVMMetrics) {
        this.printVMMetrics = printVMMetrics;
    }

    @Override
    public void run() {
        Socket socket = null;
        try {
            socket = this.socketProvider.get();
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            final long epoch = clock.time() / 1000;
            if (this.printVMMetrics) {
                printVmMetrics(epoch);
            }
            printRegularMetrics(epoch);
            writer.flush();
        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error writing to Graphite", e);
            } else {
                LOG.warn("Error writing to Graphite: {}", e.getMessage());
            }
            if (writer != null) {
                try {
                    writer.flush();
                } catch (IOException e1) {
                    LOG.error("Error while flushing writer:", e1);
                }
            }
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    LOG.error("Error while closing socket:", e);
                }
            }
            writer = null;
        }
    }

    protected void printVmMetrics(long epoch) {
    }

    protected void printRegularMetrics(long epoch) {
    }

    @Override
    public void processMeter(MetricName name, Metered meter, Long context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void processCounter(MetricName name, Counter counter, Long context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void processHistogram(MetricName name, Histogram histogram, Long context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void processTimer(MetricName name, Timer timer, Long context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void processGauge(MetricName name, Gauge<?> gauge, Long context) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class DefaultSocketProvider implements SocketProvider {

        private final String host;
        private final int port;

        public DefaultSocketProvider(String host, int port) {
            this.host = host;
            this.port = port;

        }

        @Override
        public Socket get() throws Exception {
            return new Socket(this.host, this.port);
        }

    }
}
