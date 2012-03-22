package com.yammer.metrics.examples;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.Timer;

public class DirectoryLister {
    private static final Counter counter = Metrics.newCounter(DirectoryLister.class, "directories");
    private static final Meter meter = Metrics.newMeter(DirectoryLister.class, "files", "files", TimeUnit.SECONDS);
    private static final Timer timer = Metrics.newTimer(DirectoryLister.class,
                                                       "directory-listing",
                                                       TimeUnit.MILLISECONDS,
                                                       TimeUnit.SECONDS);

    public static List<File> list(final File directory) throws Exception {
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
