package com.codahale.metrics;

import java.io.File;

@Deprecated
public class FixedNameCsvFileProvider implements CsvFileProvider {

    private final io.dropwizard.metrics5.FixedNameCsvFileProvider delegate =
            new io.dropwizard.metrics5.FixedNameCsvFileProvider();

    @Override
    public File getFile(File directory, String metricName) {
        return delegate.getFile(directory, metricName);
    }
}
