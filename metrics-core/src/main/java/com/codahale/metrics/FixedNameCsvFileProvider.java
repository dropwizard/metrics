package com.codahale.metrics;

import java.io.File;

/**
 * This implementation of the {@link CsvFileProvider} will always return the same name
 * for the same metric. This means the CSV file will grow indefinitely.
 */
public class FixedNameCsvFileProvider implements CsvFileProvider {

    @Override
    public File getFile(File directory, String metricName) {
        return new File(directory, sanitize(metricName) + ".csv");
    }

    protected String sanitize(String metricName) {
        return metricName;
    }
}
