package com.codahale.metrics;

import java.util.ArrayList;
import java.util.List;

public class Quantiles {
    public static List<Quantile> defaultQuantiles() {
        List<Quantile> result = new ArrayList<Quantile>();
        result.add(new Quantile("p50", 0.5));
        result.add(new Quantile("p75", 0.75));
        result.add(new Quantile("p95", 0.95));
        result.add(new Quantile("p98", 0.98));
        result.add(new Quantile("p99", 0.99));
        result.add(new Quantile("p999", 0.999));
        return result;
    }

    public static List<String> names(List<Quantile> quantiles) {
        List<String> result = new ArrayList<String>();
        for (Quantile quantile : quantiles) {
            result.add(quantile.getName());
        }
        return result;
    }

}
