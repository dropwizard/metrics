package com.yammer.metrics.examples;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Histogram;
import com.yammer.metrics.reporting.ConsoleReporter;

import java.io.File;
import java.util.List;
import java.util.concurrent.*;

public class ExampleRunner {
    private static final int WORKER_COUNT = 10;
    private static final BlockingQueue<File> JOBS = new LinkedBlockingQueue<File>();
    private static final ExecutorService POOL = Executors.newFixedThreadPool(WORKER_COUNT);
    private static final Counter QUEUE_DEPTH = Metrics.newCounter(ExampleRunner.class, "queue-depth");
    private static final Histogram DIRECTORY_SIZE = Metrics.newHistogram(ExampleRunner.class, "directory-size", false);

    public static class Job implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    final File file = JOBS.poll(1, TimeUnit.MINUTES);
                    QUEUE_DEPTH.dec();
                    if (file.isDirectory()) {
                        final List<File> contents = new DirectoryLister(file).list();
                        DIRECTORY_SIZE.update(contents.size());
                        QUEUE_DEPTH.inc(contents.size());
                        JOBS.addAll(contents);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void main(String[] args) throws Exception {
        ConsoleReporter.enable(10, TimeUnit.SECONDS);

        System.err.println("Scanning all files on your hard drive...");

        JOBS.add(new File("/"));
        QUEUE_DEPTH.inc();
        for (int i = 0; i < WORKER_COUNT; i++) {
            POOL.submit(new Job());
        }

        POOL.awaitTermination(10, TimeUnit.DAYS);
    }
}
