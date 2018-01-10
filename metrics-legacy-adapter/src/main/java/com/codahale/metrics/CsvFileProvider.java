package com.codahale.metrics;

import java.io.File;

@Deprecated
public interface CsvFileProvider {

    File getFile(File directory, String metricName);
}
