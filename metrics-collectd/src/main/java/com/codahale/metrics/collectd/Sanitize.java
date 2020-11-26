package com.codahale.metrics.collectd;

import java.util.Arrays;
import java.util.List;

/**
 * @see <a href="https://collectd.org/wiki/index.php/Naming_schema>Collectd naming schema</a>
 */
class Sanitize {

    static final int DEFAULT_MAX_LENGTH = 63;

    private static final char DASH = '-';
    private static final char SLASH = '/';
    private static final char NULL = '\0';
    private static final char UNDERSCORE = '_';

    private static final List<Character> INSTANCE_RESERVED = Arrays.asList(SLASH, NULL);
    private static final List<Character> NAME_RESERVED = Arrays.asList(DASH, SLASH, NULL);

    private final int maxLength;

    Sanitize(int maxLength) {
        this.maxLength = maxLength;
    }

    String name(String name) {
        return sanitize(name, NAME_RESERVED);
    }

    String instanceName(String instanceName) {
        return sanitize(instanceName, INSTANCE_RESERVED);
    }

    private String sanitize(String string, List<Character> reservedChars) {
        final StringBuilder buffer = new StringBuilder(string.length());
        final int len = Math.min(string.length(), maxLength);
        for (int i = 0; i < len; i++) {
            final char c = string.charAt(i);
            final boolean legal = ((int) c) < 128 && !reservedChars.contains(c);
            buffer.append(legal ? c : UNDERSCORE);
        }
        return buffer.toString();
    }

}
