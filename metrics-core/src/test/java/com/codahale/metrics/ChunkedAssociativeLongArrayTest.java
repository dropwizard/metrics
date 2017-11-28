package com.codahale.metrics;

import static org.assertj.core.api.BDDAssertions.then;

import org.junit.Test;

public class ChunkedAssociativeLongArrayTest {

    @Test
    public void testTrim() {
        ChunkedAssociativeLongArray array = new ChunkedAssociativeLongArray(3);
        array.put(-3, 3);
        array.put(-2, 1);
        array.put(0, 5);
        array.put(3, 0);
        array.put(9, 8);
        array.put(15, 0);
        array.put(19, 5);
        array.put(21, 5);
        array.put(34, -9);
        array.put(109, 5);

        then(array.out())
                .isEqualTo("[(-3: 3) (-2: 1) (0: 5) ]->[(3: 0) (9: 8) (15: 0) ]->[(19: 5) (21: 5) (34: -9) ]->[(109: 5) ]");
        then(array.values())
                .isEqualTo(new long[]{3, 1, 5, 0, 8, 0, 5, 5, -9, 5});
        then(array.size())
                .isEqualTo(10);

        array.trim(-2, 20);

        then(array.out())
                .isEqualTo("[(-2: 1) (0: 5) ]->[(3: 0) (9: 8) (15: 0) ]->[(19: 5) ]");
        then(array.values())
                .isEqualTo(new long[]{1, 5, 0, 8, 0, 5});
        then(array.size())
                .isEqualTo(6);

    }
}
