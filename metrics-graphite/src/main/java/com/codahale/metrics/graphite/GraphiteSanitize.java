package com.codahale.metrics.graphite;

class GraphiteSanitize {
    /** Replaces all characters from a given string that are not ascii and not alphanumeric
     *  with a dash */
    static String sanitize(String string, char replacement) {
        String replaced = replaceFrom(string, replacement);

        // Consolidate multiple dashes into a single one
        String result = replaced.replace("--", "-");
        while (!result.equals(replaced)) {
            replaced = result;
            result = replaced.replace("--", "-");
        }

        // Remove any leading or trailing dashes
        return strip(result, replacement);
    }

    /** A char matches when it is a letter or digit and it is ASCII, in Guava terminology,
     *  this would be CharMatcher.ASCII.and(CharMatcher.JAVA_LETTER_OR_DIGIT).negate() */
    private static boolean matches(char c) {
        return !(Character.isLetterOrDigit(c) && c <= '\u007f');
    }

    /** Replace all characters that we're interested in with a replacement character,
     *  heavily inspired by the same code in Guava's CharMatcher */
    private static String replaceFrom(String string, char replacement) {
        int pos = indexIn(string, 0);
        if (pos == -1) {
            return string;
        }
        char[] chars = string.toCharArray();
        chars[pos] = replacement;
        for (int i = pos + 1; i < chars.length; i++) {
            if (matches(chars[i])) {
                chars[i] = replacement;
            }
        }
        return new String(chars).trim();
    }

    /** Finds the first index (or -1) of a character we're interested in */
    private static int indexIn(String sequence, int start) {
        int length = sequence.length();
        for (int i = start; i < length; i++) {
            if (matches(sequence.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    /** Strips a given character from the beginning and end of a string,
     *  heavily inspired by Apache's StringUtils.strip
     */
    private static String strip(String str, char strip) {
        int strLen = str.length();
        int start = 0;
        int end = strLen - 1;
        while (start != strLen && str.charAt(start) == strip) {
            start++;
        }

        while (end > start && str.charAt(end) == strip) {
            end--;
        }

        return start != 0 || end != strLen - 1 ? str.substring(start, end + 1) : str;
    }
}
