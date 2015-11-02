package io.dropwizard.metrics;

import java.io.File;
import java.io.FilenameFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * This implementation of the {@link CsvFileProvider} will create a file
 * per day for each metric using the format 'metricname-yyyyMMdd.csv'.
 * It will also automatically delete old files, depending on the 'numberOfDaysToKeep' parameter.
 */
public class FilePerDayCsvFileProvider implements CsvFileProvider {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

    private Clock clock;
    private int numberOfDaysToKeep;

    public FilePerDayCsvFileProvider(Clock clock, int numberOfDaysToKeep) {
        this.clock = clock;
        this.numberOfDaysToKeep = numberOfDaysToKeep;
    }

    @Override
    public File getFile(File directory, MetricName metricName) {
        checkIfOldFilesNeedToBeDeleted(directory, metricName);

        return new File(directory, sanitize(metricName) + "_" + formatDate() + ".csv");
    }

    private void checkIfOldFilesNeedToBeDeleted(File directory, final MetricName metricName) {
        Calendar oldestToKeep = Calendar.getInstance();
        oldestToKeep.setTimeInMillis(clock.getTime());
        oldestToKeep.add(Calendar.DAY_OF_MONTH, -numberOfDaysToKeep);

        String[] fileNames = directory.list(new FileNamesOfCurrentMetricFilenameFilter(metricName));
        for (String fileName : fileNames) {
            try {
                String day = fileName.substring(fileName.lastIndexOf('_') + 1, fileName.length() - 4);
                if (DATE_FORMAT.parse(day).before(oldestToKeep.getTime())) {
                    new File(directory, fileName).delete();
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

    }

    private String formatDate() {
        return DATE_FORMAT.format(new Date(clock.getTime()));
    }

    private String sanitize(MetricName metricName) {
        return metricName.getKey();
    }

    private class FileNamesOfCurrentMetricFilenameFilter implements FilenameFilter {
        private final MetricName metricName;

        public FileNamesOfCurrentMetricFilenameFilter(MetricName metricName) {
            this.metricName = metricName;
        }

        @Override
        public boolean accept(File dir, String name) {
            return name.startsWith(sanitize(metricName));
        }
    }
}
