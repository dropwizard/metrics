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

    private int port;
    private UdpReceiver receiver;
    private Thread receiverThread;
    private BlockingQueue<ValueList> queue;

    public Receiver(int port) {
        this.port = port;
    }

    @Override
    protected void before() throws Throwable {
        queue = new LinkedBlockingQueue<ValueList>();
        receiver = new UdpReceiver(new QueueDispatcher(queue));
        receiver.setPort(port);
        start();
    }

    private void start() {
        receiverThread = new Thread(() -> {
            try {
                receiver.listen();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        receiverThread.start();
    }

    public ValueList next() throws InterruptedException {
        return queue.poll(1, TimeUnit.SECONDS);
    }

    @Override
    protected void after() {
        stop();
    }

    private void stop() {
        receiver.shutdown();
    }

    class QueueDispatcher implements Dispatcher {

        private final BlockingQueue<ValueList> queue;

        QueueDispatcher(BlockingQueue<ValueList> queue) {
            this.queue = queue;
        }

        @Override
        public void dispatch(ValueList valueList) {
            queue.offer(new ValueList(valueList));
        }

        @Override
        public void dispatch(Notification notification) {
            throw new UnsupportedOperationException();
        }
    }
}
