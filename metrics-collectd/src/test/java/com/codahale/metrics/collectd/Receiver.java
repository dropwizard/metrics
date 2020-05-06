package com.codahale.metrics.collectd;

import org.collectd.api.Notification;
import org.collectd.api.ValueList;
import org.collectd.protocol.Dispatcher;
import org.collectd.protocol.UdpReceiver;
import org.junit.rules.ExternalResource;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class Receiver extends ExternalResource {

    private final int port;

    private UdpReceiver receiver;
    private DatagramSocket socket;
    private BlockingQueue<ValueList> queue = new LinkedBlockingQueue<>();

    public Receiver(int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        socket = new DatagramSocket(null);
        socket.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), port));

        receiver = new UdpReceiver(new Dispatcher() {
            @Override
            public void dispatch(ValueList values) {
                queue.offer(new ValueList(values));
            }

            @Override
            public void dispatch(Notification notification) {
                throw new UnsupportedOperationException();
            }
        });
        receiver.setPort(port);
        new Thread(() -> {
            try {
                receiver.listen(socket);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public ValueList next() throws InterruptedException {
        return queue.poll(2, TimeUnit.SECONDS);
    }

    @Override
    protected void after() {
        receiver.shutdown();
        socket.close();
    }
}
