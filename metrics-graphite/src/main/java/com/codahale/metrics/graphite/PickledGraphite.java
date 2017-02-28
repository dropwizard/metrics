package com.codahale.metrics.graphite;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.SocketFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

/**
 * A client to a Carbon server that sends all metrics after they have been pickled in configurable sized batches
 */
public class PickledGraphite implements GraphiteSender {

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Logger LOGGER = LoggerFactory.getLogger(PickledGraphite.class);
    private final static int DEFAULT_BATCH_SIZE = 100;

    private int batchSize;
    // graphite expects a python-pickled list of nested tuples.
    private List<MetricTuple> metrics = new LinkedList<MetricTuple>();

    private final String hostname;
    private final int port;
    private final InetSocketAddress address;
    private final SocketFactory socketFactory;
    private final Charset charset;

    private Socket socket;
    private int failures;

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}. This defaults
     * to a batchSize of 100
     *
     * @param address
     *            the address of the Carbon server
     */
    public PickledGraphite(InetSocketAddress address) {
        this(address, DEFAULT_BATCH_SIZE);
    }

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}.
     *
     * @param address
     *            the address of the Carbon server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, int batchSize) {
        this(address, SocketFactory.getDefault(), batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param address
     *            the address of the Carbon server
     * @param socketFactory
     *            the socket factory
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, SocketFactory socketFactory, int batchSize) {
        this(address, socketFactory, UTF_8, batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given character set.
     *
     * @param address
     *            the address of the Carbon server
     * @param socketFactory
     *            the socket factory
     * @param charset
     *            the character set used by the server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(InetSocketAddress address, SocketFactory socketFactory, Charset charset, int batchSize) {
        this.address = address;
        this.hostname = null;
        this.port = -1;
        this.socketFactory = socketFactory;
        this.charset = charset;
        this.batchSize = batchSize;
    }

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}. This defaults
     * to a batchSize of 100
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     */
    public PickledGraphite(String hostname, int port) {
        this(hostname, port, DEFAULT_BATCH_SIZE);
    }

    /**
     * Creates a new client which connects to the given address using the default {@link SocketFactory}.
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(String hostname, int port, int batchSize) {
        this(hostname, port, SocketFactory.getDefault(), batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory.
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     * @param socketFactory
     *            the socket factory
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(String hostname, int port, SocketFactory socketFactory, int batchSize) {
        this(hostname, port, socketFactory, UTF_8, batchSize);
    }

    /**
     * Creates a new client which connects to the given address and socket factory using the given character set.
     *
     * @param hostname
     *            the hostname of the Carbon server
     * @param port
     *            the port of the Carbon server
     * @param socketFactory
     *            the socket factory
     * @param charset
     *            the character set used by the server
     * @param batchSize
     *            how many metrics are bundled into a single pickle request to graphite
     */
    public PickledGraphite(String hostname, int port, SocketFactory socketFactory, Charset charset, int batchSize) {
        this.address = null;
        this.hostname = hostname;
        this.port = port;
        this.socketFactory = socketFactory;
        this.charset = charset;
        this.batchSize = batchSize;
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }
        InetSocketAddress address = this.address;
        if (address == null) {
            address = new InetSocketAddress(hostname, port);
        }
        if (address.getAddress() == null) {
            throw new UnknownHostException(address.getHostName());
        }

        this.socket = socketFactory.createSocket(address.getAddress(), address.getPort());
    }

    @Override
    public boolean isConnected() {
      if (socket != null && socket.isConnected() && !socket.isClosed()) {
        // the above checks isn't enough to identify semi open connections
        // See RFC793 figure 11 for the scenario which this is trying to address
        // if socket is open, it should be fine to send a byte right?

        try {
          OutputStream os = socket.getOutputStream();
          os.write(0);
          return true;
        } catch (IOException ioe) {
          try {
            socket.close();
          } catch (IOException ioe2) {
          }
          finally {
            this.socket = null;
          }
        }
      }
      return false;
    }

    /**
     * Convert the metric to a python tuple of the form:
     * <p/>
     * (timestamp, (name, value))
     * <p/>
     * And add it to the list of metrics. If we reach the batch size, write them out.
     *
     * @param name
     *            the name of the metric
     * @param value
     *            the value of the metric
     * @param timestamp
     *            the timestamp of the metric
     * @throws IOException
     *             if there was an error sending the metric
     */
    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        metrics.add(new MetricTuple(sanitize(name), timestamp, sanitize(value)));

        if (metrics.size() >= batchSize) {
            writeMetrics();
        }
    }

    @Override
    public void flush() throws IOException {
        writeMetrics();
    }

    @Override
    public void close() throws IOException {
        try {
            flush();
            socket.close();
        } catch (IOException ex) {
            // if flush() is tried over a semi closed socket then
            // IOException will be thrown. We want to explicitly
            // release the resource too.
            socket.close();
        } finally {
            this.socket = null;
        }
    }

    @Override
    public int getFailures() {
        return failures;
    }

    /**
     * 1. Run the pickler script to package all the pending metrics into a single message
     * 2. Send the message to graphite
     * 3. Clear out the list of metrics
     */
    private void writeMetrics() throws IOException {
        if (metrics.size() > 0) {
            try {
                byte[] payload = pickleMetrics(metrics);
                byte[] header = ByteBuffer.allocate(4).putInt(payload.length).array();

                @SuppressWarnings("resource")
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(header);
                outputStream.write(payload);
                outputStream.flush();

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrote {} metrics", metrics.size());
                }
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
    private final char
            MARK = '(',
            STOP = '.',
            LONG = 'L',
            STRING = 'S',
            APPEND = 'a',
            LIST = 'l',
            TUPLE = 't',
            QUOTE = '\'',
            LF = '\n';

    /**
     * See: http://readthedocs.org/docs/graphite/en/1.0/feeding-carbon.html
     *
     * @throws IOException
     */
    byte[] pickleMetrics(List<MetricTuple> metrics) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(metrics.size() * 75); // Extremely rough estimate of 75 bytes per message
        Writer pickled = new OutputStreamWriter(out, charset);

        pickled.append(MARK);
        pickled.append(LIST);

        for (MetricTuple tuple : metrics) {
            // start the outer tuple
            pickled.append(MARK);

            // the metric name is a string.
            pickled.append(STRING);
            // the single quotes are to match python's repr("abcd")
            pickled.append(QUOTE);
            pickled.append(tuple.name);
            pickled.append(QUOTE);
            pickled.append(LF);

            // start the inner tuple
            pickled.append(MARK);

            // timestamp is a long
            pickled.append(LONG);
            pickled.append(Long.toString(tuple.timestamp));
            // the trailing L is to match python's repr(long(1234))
            pickled.append(LONG);
            pickled.append(LF);

            // and the value is a string.
            pickled.append(STRING);
            pickled.append(QUOTE);
            pickled.append(tuple.value);
            pickled.append(QUOTE);
            pickled.append(LF);

            pickled.append(TUPLE); // inner close
            pickled.append(TUPLE); // outer close

            pickled.append(APPEND);
        }

        // every pickle ends with STOP
        pickled.append(STOP);

        pickled.flush();

        return out.toByteArray();
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

    protected String sanitize(String s) {
        return GraphiteSanitize.sanitize(s, '-');
    }

}
