package com.yammer.metrics.reporting;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class GangliaMessageTest {
    @Test
    public void canAddInt() {
        final int bytesToWrite = 4; //integer
        final byte[] buffer = new byte[bytesToWrite];
        final byte[] expecteds = new byte[]{0, 0, 2, (byte) 166};

        final GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addInt(678);

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }

    @Test
    public void canAddString() {
        final int bytesToWrite = 4 + 4; //integer + message
        final byte[] buffer = new byte[bytesToWrite];
        final byte[] expecteds = new byte[]{0, 0, 0, 4, 't', 'e', 's', 't'};

        final GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addString("test");

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }

    @Test
    public void canAddPaddedString() {
        final int bytesToWrite = 4 + 5 + 3; //integer + message + padding
        final byte[] buffer = new byte[bytesToWrite];
        final byte[] expecteds = new byte[]{0, 0, 0, 5, 't', 'e', 's', 't', 's', 0, 0, 0};

        final GangliaMessage message = new GangliaMessage(null, buffer, null);

        message.addString("tests");

        assertArrayEquals(expecteds, buffer);
        assertEquals(bytesToWrite, message.getOffset());
    }
}
