package com.yammer.metrics.librato;

import org.junit.Test;

import java.util.*;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * User: mihasya
 * Date: 6/25/12
 * Time: 11:10 PM
 */
public class LastPassSanitizerTest {
    /**
     * Take a string that's a little too long even without the special chars; verify that the extra gets lopped off the front
     * @throws Exception
     */
    @Test
    public void testRemovingIllegalMethods() throws Exception {
        LibratoUtil.Sanitizer sanitizer = LibratoUtil.lastPassSanitizer;
        String important = "reallyclutchinfo";

        List<String> illegalCharacters = new ArrayList<String>();
        illegalCharacters.add("$");
        illegalCharacters.add("]");
        illegalCharacters.add("[");
        illegalCharacters.add("*");
        illegalCharacters.add("+");

        StringBuilder testStringBuilder = new StringBuilder();

        testStringBuilder.append("com.less.important.nonunique.prefix.");

        for (int i = 0; i < 16; i++) {
            Collections.shuffle(illegalCharacters);
            testStringBuilder.append(important);
            testStringBuilder.append(illegalCharacters.get(0));
        }

        String key = testStringBuilder.toString();

        String sanitized = sanitizer.apply(key);

        assertEquals(256, sanitized.length());
        assertEquals("reallyclutchinfo", sanitized.substring(0, 16));
        assertEquals("reallyclutchinfo", sanitized.substring(240, 256));
        for (String illegalCharacter : illegalCharacters) {
            assertFalse("Key still contains illegal character " + illegalCharacter, sanitized.contains(illegalCharacter));
        }
    }
}
