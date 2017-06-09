package com.codahale.metrics.graphite;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.failBecauseExceptionWasNotThrown;


import static org.mockito.Mockito.*;

public class GraphiteRabbitMQTest
{
    private final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    private final Connection connection = mock(Connection.class);
    private final Channel channel = mock(Channel.class);

    private final ConnectionFactory bogusConnectionFactory = mock(ConnectionFactory.class);
    private final Connection bogusConnection = mock(Connection.class);
    private final Channel bogusChannel = mock(Channel.class);

    private final GraphiteRabbitMQ graphite = new GraphiteRabbitMQ(connectionFactory, "graphite");

    private static final Charset UTF_8 = Charset.forName("UTF-8");

    @Before
    public void setUp() throws Exception {
        when(connectionFactory.newConnection()).thenReturn(connection);
        when(connection.createChannel()).thenReturn(channel);
        when(connection.isOpen()).thenReturn(true);

        when(bogusConnectionFactory.newConnection()).thenReturn(bogusConnection);
        when(bogusConnection.createChannel()).thenReturn(bogusChannel);
        doThrow(new IOException())
                .when(bogusChannel)
                .basicPublish(anyString(), anyString(), any(), any(byte[].class));
    }

    @Test
    public void shouldConnectToGraphiteServer() throws Exception {
        graphite.connect();

        verify(connectionFactory, atMost(1)).newConnection();
        verify(connection, atMost(1)).createChannel();

    }

    @Test
    public void measuresFailures() throws Exception {
        final GraphiteRabbitMQ graphite = new GraphiteRabbitMQ(bogusConnectionFactory, "graphite");
        graphite.connect();

        try {
            graphite.send("name", "value", 0);
            failBecauseExceptionWasNotThrown(IOException.class);
        } catch (IOException e) {
            assertThat(graphite.getFailures()).isEqualTo(1);
        }
    }

    @Test
    public void shouldDisconnectsFromGraphiteServer() throws Exception {
        graphite.connect();
        graphite.close();

        verify(connection).close();
    }

    @Test
    public void shouldNotConnectToGraphiteServerMoreThenOnce() throws Exception {
        graphite.connect();
        try {
            graphite.connect();
            failBecauseExceptionWasNotThrown(IllegalStateException.class);
        } catch (IllegalStateException e) {
            assertThat(e.getMessage()).isEqualTo("Already connected");
       }
    }

    @Test
    public void shouldSendMetricsToGraphiteServer() throws Exception {
        graphite.connect();
        graphite.send("name", "value", 100);

        String expectedMessage = "name value 100\n";

        verify(channel, times(1)).basicPublish("graphite", "name", null, expectedMessage.getBytes(UTF_8));

        assertThat(graphite.getFailures()).isZero();
    }

    @Test
    public void shouldSanitizeAndSendMetricsToGraphiteServer() throws Exception {
        graphite.connect();
        graphite.send("name to sanitize", "value to sanitize", 100);

        String expectedMessage = "name-to-sanitize value-to-sanitize 100\n";

        verify(channel, times(1)).basicPublish("graphite", "name-to-sanitize", null, expectedMessage.getBytes(UTF_8));

        assertThat(graphite.getFailures()).isZero();
    }

    @Test
    public void shouldFailWhenGraphiteHostUnavailable() throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("some-unknown-host");

        GraphiteRabbitMQ unavailableGraphite = new GraphiteRabbitMQ(connectionFactory, "graphite");

        try {
            unavailableGraphite.connect();
            failBecauseExceptionWasNotThrown(UnknownHostException.class);
        } catch (Exception e) {
            assertThat(e.getMessage())
                    .isEqualTo("some-unknown-host");
        }
    }
}
