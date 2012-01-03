package com.yammer.metrics.stats;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A statistically representative sample of a data stream.
 */
public interface Sample {
    /**
     * Clears all recorded values.
     */
    void clear();

    /**
     * Returns the number of values recorded.
     *
     * @return the number of values recorded
     */
    int size();

    /**
     * Adds a new recorded value to the sample.
     *
     * @param value a new recorded value
     */
    void update(long value);

    /**
     * Returns a snapshot of the sample's values.
     *
     * @return a snapshot of the sample's values
     */
    Snapshot getSnapshot();

    /**
     * Returns a copy of the sample's values.
     *
     * @return a copy of the sample's values
     */
    List<Long> values();

    /**
     * Writes the values of the sample to the given file.
     *
     * @param output the file to which the values will be written
     * @throws IOException if there is an error writing the values
     */
    void dump(File output) throws IOException;
}
