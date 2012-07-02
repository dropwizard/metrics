package com.yammer.metrics.librato;

import java.util.HashMap;
import java.util.Map;

/**
 * User: mihasya
 * Date: 6/17/12
 * Time: 10:06 PM
 * a class for representing a gauge reading that might come from multiple samples
 *
 * See http://dev.librato.com/v1/post/metrics for why some fields are optional
 */
public class MultiSampleGaugeMeasurement implements Measurement {
    private final String name;
    private final Number count;
    private final Number sum;
    private final Number max;
    private final Number min;
    private final Number sum_squares;

    public MultiSampleGaugeMeasurement(String name, Number count, Number sum, Number max, Number min, Number sum_squares) {
        this.name = name;
        this.count = count;
        this.sum = sum;
        this.max = max;
        this.min = min;
        this.sum_squares = sum_squares;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Map<String, Number> toMap() {
        Map<String, Number> result = new HashMap<String, Number>(5);
        result.put("count", count);
        result.put("sum", sum);
        if (max != null) {
            result.put("max", max);
        }
        if (min != null) {
            result.put("min", min);
        }
        if (sum_squares != null) {
            result.put("sum_squares", sum_squares);
        }
        return result;
    }
}
