package com.codahale.metrics.graphite.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

/**
 * A rabbit-mq client to a Carbon server.
 */
public class GraphiteRabbitMQ implements GraphiteClient {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    private static final Integer DEFAULT_RABBIT_CONNECTION_TIMEOUT_MS = 500;
    private static final Integer DEFAULT_RABBIT_SOCKET_TIMEOUT_MS = 5000;
    private static final Integer DEFAULT_RABBIT_REQUESTED_HEARTBEAT_SEC = 10;

    private ConnectionFactory connectionFactory;
    private Connection connection;
    private Channel channel;
    private String exchange;

    private int failures;

    /**
     * Creates a new client with a given a {@link com.rabbitmq.client.ConnectionFactory} and an amqp exchange
     *
     * @param connectionFactory the {@link com.rabbitmq.client.ConnectionFactory} used to establish connection and publish to graphite server
     * @param exchange          the amqp exchange
     */
    public GraphiteRabbitMQ(final ConnectionFactory connectionFactory, final String exchange) {
        this.connectionFactory = connectionFactory;
        this.exchange = exchange;
    }

    /**
     * Creates a new client given connection details
     *
     * @param rabbitHost     the rabbitmq server host
     * @param rabbitPort     the rabbitmq server port
     * @param rabbitUsername the rabbitmq server username
     * @param rabbitPassword the rabbitmq server password
     * @param exchange       the amqp exchange
     */
    public GraphiteRabbitMQ(
            final String rabbitHost,
            final Integer rabbitPort,
            final String rabbitUsername,
            final String rabbitPassword,
            final String exchange) {

        this(rabbitHost,
                rabbitPort,
                rabbitUsername,
                rabbitPassword,
                exchange,
                DEFAULT_RABBIT_CONNECTION_TIMEOUT_MS,
                DEFAULT_RABBIT_SOCKET_TIMEOUT_MS,
                DEFAULT_RABBIT_REQUESTED_HEARTBEAT_SEC);
    }

    /**
     * Creates a new client given connection details
     *
     * @param rabbitHost                        the rabbitmq server host
     * @param rabbitPort                        the rabbitmq server port
     * @param rabbitUsername                    the rabbitmq server username
     * @param rabbitPassword                    the rabbitmq server password
     * @param exchange                          the amqp exchange
     * @param rabbitConnectionTimeoutMS         the connection timeout in milliseconds
     * @param rabbitSocketTimeoutMS             the socket timeout in milliseconds
     * @param rabbitRequestedHeartbeatInSeconds the hearthbeat in seconds
     */
    public GraphiteRabbitMQ(
            final String rabbitHost,
            final Integer rabbitPort,
            final String rabbitUsername,
            final String rabbitPassword,
            final String exchange,
            final Integer rabbitConnectionTimeoutMS,
            final Integer rabbitSocketTimeoutMS,
            final Integer rabbitRequestedHeartbeatInSeconds) {

        this.exchange = exchange;

        this.connectionFactory = new ConnectionFactory() {
            @Override
            protected void configureSocket(Socket socket) throws IOException {
                super.configureSocket(socket);
                socket.setSoTimeout(rabbitSocketTimeoutMS);
            }
        };

        connectionFactory.setConnectionTimeout(rabbitConnectionTimeoutMS);
        connectionFactory.setRequestedHeartbeat(rabbitRequestedHeartbeatInSeconds);
        connectionFactory.setHost(rabbitHost);
        connectionFactory.setPort(rabbitPort);
        connectionFactory.setUsername(rabbitUsername);
        connectionFactory.setPassword(rabbitPassword);
    }

    /**
     * (non-Javadoc)
     *
     * @throws IllegalStateException if the client is already connected
     * @see com.codahale.metrics.graphite.client.GraphiteClient#connect()
     */
    @Override
    public void connect() throws IllegalStateException, IOException {
        if (connection != null && connection.isOpen()) {
            throw new IllegalStateException("Already connected");
        }

        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
    }

    /**
     * (non-Javadoc)
     *
     * @see com.codahale.metrics.graphite.client.GraphiteClient#send(String, String, long)
     */
    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        try {
            final String sanitizedName = sanitize(name);
            final String sanitizedValue = sanitize(value);

            final String message =
                    new StringBuilder()
                            .append(sanitizedName).append(' ')
                            .append(sanitizedValue).append(' ')
                            .append(Long.toString(timestamp)).append('\n').toString();

            channel.basicPublish(exchange, sanitizedName, null, message.getBytes(UTF_8));
        } catch (IOException e) {
            failures++;
            throw e;
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.codahale.metrics.graphite.client.GraphiteClient#close()
     */
    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    /**
     * (non-Javadoc)
     *
     * @see com.codahale.metrics.graphite.client.GraphiteClient#sanitize(String)
     */
    @Override
    public String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }

    public int getFailures() {
        return failures;
    }
}
