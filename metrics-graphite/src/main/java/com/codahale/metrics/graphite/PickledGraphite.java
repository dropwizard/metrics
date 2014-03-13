package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * A client to a Carbon server that sends all metrics after they have been pickled in configurable sized batches
 */
public class PickledGraphite extends Graphite {

    private static final Logger LOGGER = LoggerFactory.getLogger(PickledGraphite.class);
    final static int DEFAULT_BATCH_SIZE = 100;

    int batchSize;
    // graphite expects a python-pickled list of nested tuples.
    List<MetricTuple> metrics = new LinkedList<MetricTuple>();

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}. This defaults to a batchSize of 100
     *
     * @param address the address of the Carbon server
     */
    public PickledGraphite(InetSocketAddress address) {
        this(address, DEFAULT_BATCH_SIZE);
    }

    /**
     * Creates a new client which connects to the given address using the default
     * {@link SocketFactory}.
     *
     * @param address the address of the Carbon server
     * @param batchSize     how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, int batchSize) {
        this(address, SocketFactory.getDefault(), batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     * @param batchSize     how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, SocketFactory socketFactory, int batchSize) {
        this(address, socketFactory, UTF_8, batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given
     * character set.
     *
     * @param address       the address of the Carbon server
     * @param socketFactory the socket factory
     * @param charset       the character set used by the server
     * @param batchSize     how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, SocketFactory socketFactory, Charset charset, int batchSize) {
        super(address, socketFactory, charset);
        this.batchSize = batchSize;
    }

    /**
     * Convert the metric to a python tuple of the form:
     * <p/>
     * (timestamp, (name, value))
     * <p/>
     * And add it to the list of metrics.
     * If we reach the batch size, write them out.
     *
     * @param name      the name of the metric
     * @param value     the value of the metric
     * @param timestamp the timestamp of the metric
     * @throws IOException if there was an error sending the metric
     */
    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        metrics.add(new MetricTuple(sanitize(name), timestamp, sanitize(value)));

        if (metrics.size() >= batchSize) {
            writeMetrics();
        }
    }

    @Override
    public void close() throws IOException {
        writeMetrics();
        super.close();
    }

    /**
     * 1. Run the pickler script to package all the pending metrics into a single message
     * 2. Send the message to graphite
     * 3. Clear out the list of metrics
     */
    private void writeMetrics() throws IOException {
        if (metrics.size() > 0) {
            try {
                String payload = pickleMetrics(metrics);
                int length = payload.length();
                byte[] header = ByteBuffer.allocate(4).putInt(length).array();
                socket.getOutputStream().write(header);
                writer.write(payload);
                writer.flush();
                LOGGER.debug("Wrote {} metrics", metrics.size());
            } catch (IOException e) {
                this.failures++;
                throw e;
            } finally {
                // if there was an error, we might miss some data. for now, drop those on the floor and
                // try to keep going.
                metrics.clear();
            }

        }
    }

    /**
     * Minimally necessary pickle opcodes.
     */
    char
        MARK = '(',
        STOP = '.',
        LONG = 'L',
        STRING = 'S',
        APPEND = 'a',
        LIST = 'l',
        TUPLE = 't';

    /**
     * See: http://readthedocs.org/docs/graphite/en/1.0/feeding-carbon.html
     */
    String pickleMetrics(List<MetricTuple> metrics) {

        StringBuilder pickled = new StringBuilder();
        pickled.append(MARK);
        pickled.append(LIST);

        for (MetricTuple tuple : metrics) {
            // start the outer tuple
            pickled.append(MARK);

            // the metric name is a string.
            pickled.append(STRING);
            // the single quotes are to match python's repr("abcd")
            pickled.append('\'');
            pickled.append(tuple.name);
            pickled.append('\'');
            pickled.append('\n');

            // start the inner tuple
            pickled.append(MARK);

            // timestamp is a long
            pickled.append(LONG);
            pickled.append(tuple.timestamp);
            // the trailing L is to match python's repr(long(1234))
            pickled.append('L');
            pickled.append('\n');

            // and the value is a string.
            pickled.append(STRING);
            pickled.append('\'');
            pickled.append(tuple.value);
            pickled.append('\'');
            pickled.append('\n');

            pickled.append(TUPLE); // inner close
            pickled.append(TUPLE); // outer close

            pickled.append(APPEND);
        }

        // every pickle ends with STOP
        pickled.append(STOP);
        return pickled.toString();
    }

    static class MetricTuple {
        String name;
        long timestamp;
        String value;

        MetricTuple(String name, long timestamp, String value) {
            this.name = name;
            this.timestamp = timestamp;
            this.value = value;
        }
    }
}
