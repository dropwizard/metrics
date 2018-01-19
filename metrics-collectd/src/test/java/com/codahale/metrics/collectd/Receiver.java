package com.codahale.metrics.collectd;

import org.collectd.api.Notification;
import org.collectd.api.ValueList;
import org.collectd.protocol.Dispatcher;
import org.collectd.protocol.UdpReceiver;
import org.junit.rules.ExternalResource;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public final class Receiver extends ExternalResource {

    private final int port;

    private UdpReceiver receiver;
    private BlockingQueue<ValueList> queue = new LinkedBlockingQueue<>();

    public Receiver(int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
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
                receiver.listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public ValueList next() throws InterruptedException {
        return queue.poll(1, TimeUnit.SECONDS);
    }

    @Override
    protected void after() {
        receiver.shutdown();
    }
}
