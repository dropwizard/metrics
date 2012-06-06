package com.yammer.metrics.examples;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class DirectoryLister {
    private final MetricsRegistry registry = Metrics.defaultRegistry();
    private final Counter counter = registry.newCounter(getClass(), "directories");
    private final Meter meter = registry.newMeter(getClass(), "files", "files", TimeUnit.SECONDS);
    private final Timer timer = registry.newTimer(getClass(),
                                                  "directory-listing",
                                                  TimeUnit.MILLISECONDS,
                                                  TimeUnit.SECONDS);
    private final File directory;

    public DirectoryLister(File directory) {
        this.directory = directory;
    }

    public List<File> list() throws Exception {
        counter.inc();
        final File[] list = timer.time(new Callable<File[]>() {
            @Override
            public File[] call() throws Exception {
                return directory.listFiles();
            }
        });
        counter.dec();

        if (list == null) {
            return Collections.emptyList();
        }

        final List<File> result = new ArrayList<File>(list.length);
        for (File file : list) {
            meter.mark();
            result.add(file);
        }
        return result;
    }
}
