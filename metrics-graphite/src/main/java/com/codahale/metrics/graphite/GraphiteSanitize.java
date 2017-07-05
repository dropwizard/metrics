package com.codahale.metrics.graphite;

import java.util.regex.Pattern;

class GraphiteSanitize {

    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    private static final String DASH = "-";

    /**
     * Trims the string and replaces all whitespace characters with the provided symbol
     */
    static String sanitize(String string) {
        return WHITESPACE.matcher(string.trim()).replaceAll(DASH);
    }
}
