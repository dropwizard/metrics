package com.yammer.metrics.librato;

import org.junit.Before;
import org.junit.Test;

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
        String testString = "ccaaaaaaaaaa$$$aaaaaaa$aaaaaaaaaaaaa$aaaaaaaaaaaaaaa[aaaaaaaa][aaaaaaaaaaa(aaaaa**aaaa(((aaaaaaaaa++++aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaab";
        String sanitized = sanitizer.apply(testString);

        assertEquals(256, sanitized.length());
        assertEquals("a", sanitized.substring(0, 1));
        assertEquals("b", sanitized.substring(255, 256));
        assertFalse(sanitized.contains("["));
        assertFalse(sanitized.contains("]"));
        assertFalse(sanitized.contains("$"));
        assertFalse(sanitized.contains("("));
        assertFalse(sanitized.contains(")"));
        assertFalse(sanitized.contains("*"));
        System.out.println(sanitized);
    }
}
