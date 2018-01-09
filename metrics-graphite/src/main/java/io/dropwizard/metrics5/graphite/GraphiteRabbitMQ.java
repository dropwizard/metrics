package io.dropwizard.metrics5.graphite;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DefaultSocketConfigurator;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeoutException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * A rabbit-mq client to a Carbon server.
 */
public class GraphiteRabbitMQ implements GraphiteSender {

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

        this.connectionFactory = new ConnectionFactory();

        connectionFactory.setSocketConfigurator(new DefaultSocketConfigurator() {
            @Override
            public void configure(Socket socket) throws IOException {
                super.configure(socket);
                socket.setSoTimeout(rabbitSocketTimeoutMS);
            }
        });

        connectionFactory.setConnectionTimeout(rabbitConnectionTimeoutMS);
        connectionFactory.setRequestedHeartbeat(rabbitRequestedHeartbeatInSeconds);
        connectionFactory.setHost(rabbitHost);
        connectionFactory.setPort(rabbitPort);
        connectionFactory.setUsername(rabbitUsername);
        connectionFactory.setPassword(rabbitPassword);
    }

    @Override
    public void connect() throws IllegalStateException, IOException {
        if (isConnected()) {
            throw new IllegalStateException("Already connected");
        }

        try {
            connection = connectionFactory.newConnection();
        } catch (TimeoutException e) {
            throw new IllegalStateException(e);
        }
        channel = connection.createChannel();
    }

    @Override
    public boolean isConnected() {
        return connection != null && connection.isOpen();
    }

    @Override
    public void send(String name, String value, long timestamp) throws IOException {
        try {
            final String sanitizedName = sanitize(name);
            final String sanitizedValue = sanitize(value);

            final String message = sanitizedName + ' ' + sanitizedValue + ' ' + Long.toString(timestamp) + '\n';
            channel.basicPublish(exchange, sanitizedName, null, message.getBytes(UTF_8));
        } catch (IOException e) {
            failures++;
            throw e;
        }
    }

    @Override
    public void flush() throws IOException {
        // Nothing to do
    }

    @Override
    public void close() throws IOException {
        if (connection != null) {
            connection.close();
        }
    }

    @Override
    public int getFailures() {
        return failures;
    }

    public String sanitize(String s) {
        return GraphiteSanitize.sanitize(s);
    }

}
