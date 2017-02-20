package io.dropwizard.metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A MetricNameFormatter is used to convert a {@link MetricName} into a string.
 */
public interface MetricNameFormatter {

    /**
     * Format the given {@link MetricName} into a String
     * @param name the MetricName to format
     * @return the formatted string from the MetricName
     */
    public String formatMetricName(MetricName name);
    
    /**
     * A {@link MetricNameFormatter} that will only use the name part of the {@link MetricName} and
     * ignore all tags
     */
    public static final MetricNameFormatter NAME_ONLY = name -> name.getKey();
    
    /**
     * A {@link MetricNameFormatter} that will invoke {@link MetricName#toString()}
     */
    public static final MetricNameFormatter METRIC_NAME_TOSTRING =name -> name.toString();
    
    /**
     * A {@link MetricNameFormatter} that will append tag names and values to the metric string.
     * The tags are sorted by tag key and then appended in sorted order to the name.
     * For example: <code>foo.bar.time[m=b,d=e]</code> will result in <code>foo.bar.time.d.e.m.b</code>
     */
    public static final MetricNameFormatter APPEND_TAGS = name -> {
        StringBuilder sb = new StringBuilder(name.getKey());
        Map<String,String> tags = name.getTags();
        List<String> tagNames = new ArrayList<>(tags.keySet());
        Collections.sort(tagNames);
        tagNames.forEach( tag -> {
            sb.append(".");
            sb.append(tag);
            sb.append(".");
            sb.append(tags.get(tag));
        });
        
        return sb.toString();
    };
    
    /**
     * A {@link MetricNameFormatter} that will append only the values to the metric string. The 
     * tag values are sorted and then appended in sorted order.
     * For example: <code>foo.bar.time[m=b,d=e,f=a]</code> will result in <code>foo.bar.time.a.b.e</code>
     */
    public static final MetricNameFormatter APPEND_TAG_VALUES = name -> {
        StringBuilder sb = new StringBuilder(name.getKey());
        List<String> tagValues = new ArrayList<>(name.getTags().values());
        Collections.sort(tagValues);
        tagValues.forEach( value -> {
            sb.append(".");
            sb.append(value);
        });
        
        return sb.toString();
    };
    
}
