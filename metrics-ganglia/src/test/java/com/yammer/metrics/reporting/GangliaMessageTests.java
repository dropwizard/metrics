package com.yammer.metrics.reporting;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GangliaMessageTests {
    @Test
    public void canAddInt() {
        int bytesToWrite = 4; //integer
        byte[] buffer = new byte[bytesToWrite];
        byte[] expecteds = new byte[]{0, 0, 2, (byte) 166};

        GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addInt(678);

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }

    @Test
    public void canAddString() {
        int bytesToWrite = 4 + 4; //integer + message
        byte[] buffer = new byte[bytesToWrite];
        byte[] expecteds = new byte[]{0, 0, 0, 4, 't', 'e', 's', 't'};

        GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addString("test");

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }

    @Test
    public void canAddPaddedString() {
        int bytesToWrite = 4 + 5 + 3; //integer + message + padding
        byte[] buffer = new byte[bytesToWrite];
        byte[] expecteds = new byte[]{0, 0, 0, 5, 't', 'e', 's', 't', 's', 0, 0, 0};

        GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addString("tests");

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }
}
