package io.dropwizard.metrics;

import java.io.File;

/**
 * This implementation of the {@link CsvFileProvider} will always return the same name
 * for the same metric. This means the CSV file will grow indefinitely.
 */
public class FixedNameCsvFileProvider implements CsvFileProvider {
    @Override
    public File getFile(File directory, MetricName metricName) {
        return new File(directory, sanitize(metricName) + ".csv");
    }

    private String sanitize(MetricName metricName) {
        return metricName.getKey();
    }
}
