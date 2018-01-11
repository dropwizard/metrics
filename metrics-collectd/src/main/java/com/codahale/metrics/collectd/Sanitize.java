package com.codahale.metrics.collectd;

import java.util.Arrays;
import java.util.List;

/**
 * @see <a href="https://collectd.org/wiki/index.php/Naming_schema>Collectd naming schema</a>
 */
final class Sanitize {

    private static final int MAX_LENGTH = 63;

    private static final char DASH = '-';
    private static final char SLASH = '/';
    private static final char NULL = '\0';
    private static final char UNDERSCORE = '_';

    private static final List<Character> INSTANCE_RESERVED = Arrays.asList(SLASH, NULL);
    private static final List<Character> NAME_RESERVED = Arrays.asList(DASH, SLASH, NULL);

    static String name(String name) {
        return sanitize(name, NAME_RESERVED);
    }

    static String instanceName(String instanceName) {
        return sanitize(instanceName, INSTANCE_RESERVED);
    }

    private static String sanitize(String string, List<Character> reservedChars) {
        StringBuilder buffer = new StringBuilder(string.length());
        int len = Math.min(string.length(), MAX_LENGTH);
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            boolean legal = ((int) c) < 128 && !reservedChars.contains(c);
            buffer.append(legal ? c : UNDERSCORE);
        }
        return buffer.toString();
    }

}
