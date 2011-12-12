package com.yammer.metrics.jdbi.strategies;

import java.util.regex.Pattern;

import com.yammer.metrics.core.MetricName;

public class StatementName
{
    /** Characters safe to be used in JMX names. */
    private static final Pattern JMX_SAFE_CHARS = Pattern.compile("[^a-zA-Z0-9_\\.-]");

    public static MetricName getJmxSafeName(String groupName, String typeName, String statementName) {
        return new MetricName(getJmxSafeName(groupName), getJmxSafeName(typeName), getJmxSafeName(statementName));
    }

    /**
     * Turns an arbitrary string into a JMX safe name.
     */
    private static final String getJmxSafeName(final String name)
    {
        final String result = JMX_SAFE_CHARS.matcher(name).replaceAll("_");

        if (result == null || result.length() == 0) {
            return "_";
        }

        return (Character.isDigit(result.charAt(0))) ? "_" + result : result;
    }
}
