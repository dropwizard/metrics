package com.codahale.metrics.collectd;

/**
 * @see <a href="https://collectd.org/wiki/index.php/Naming_schema>Collectd naming schema</a>
 */
final class Sanitize {

    private static final int MAX_LENGTH = 63;

    private static final char DASH = '-';
    private static final char SLASH = '/';
    private static final char NULL = '\0';
    private static final char UNDERSCORE = '_';

    static String name(String name) {
        return sanitize(name, DASH, SLASH, NULL);
    }

    static String instanceName(String instanceName) {
        return sanitize(instanceName, SLASH, NULL);
    }

    private static String sanitize(String string, char... reservedChars) {
        StringBuilder buffer = new StringBuilder(string.length());
        int len = Math.min(string.length(), MAX_LENGTH);
        for (int i = 0; i < len; i++) {
            char c = string.charAt(i);
            boolean legal = ((int) c) < 128 && !contained(c, reservedChars);
            buffer.append(legal ? c : UNDERSCORE);
        }
        return buffer.toString();
    }

    private static boolean contained(char c, char... chars) {
        for (int i = 0; i < chars.length; i++) {
            if (c == chars[i]) {
                return true;
            }
        }
        return false;
    }

}
