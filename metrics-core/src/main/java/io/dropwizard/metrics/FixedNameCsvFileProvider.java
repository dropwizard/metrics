package io.dropwizard.metrics;

import java.io.File;

/**
 * This implementation of the {@link CsvFileProvider} will always return the same name
 * for the same metric. This means the CSV file will grow indefinitely.
 */
public class FixedNameCsvFileProvider implements CsvFileProvider {
    private static final MetricNameFormatter nameFormatter = MetricNameFormatter.APPEND_TAGS;
    @Override
    public File getFile(File directory, MetricName metricName) {
        return new File(directory, sanitize(metricName) + ".csv");
    }

    private String sanitize(MetricName metricName) {
        //Forward slash character is definitely illegal in both Windows and Linux
        //https://msdn.microsoft.com/en-us/library/windows/desktop/aa365247(v=vs.85).aspx
        final String name = nameFormatter.formatMetricName(metricName);
        final String sanitizedName = name.replaceFirst("^/","").replaceAll("/",".");
        return sanitizedName;
    }
}
