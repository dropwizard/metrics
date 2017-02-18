package com.codahale.metrics;

import java.io.File;

/**
 * This interface allows a pluggable implementation of what file names
 * the {@link CsvReporter} will write to.
 */
public interface CsvFileProvider {

    File getFile(File directory, String metricName);
}
