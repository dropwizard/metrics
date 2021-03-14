package com.codahale.metrics;

import org.junit.Test;

import static org.assertj.core.api.BDDAssertions.then;

public class ChunkedAssociativeDoubleArrayTest {

    @Test
    public void testTrim() {
        ChunkedAssociativeDoubleArray array = new ChunkedAssociativeDoubleArray(3);
        array.put(-3, 3D);
        array.put(-2, 1D);
        array.put(0, 5D);
        array.put(3, 0D);
        array.put(9, 8D);
        array.put(15, 0D);
        array.put(19, 5D);
        array.put(21, 5D);
        array.put(34, -9D);
        array.put(109, 5D);

        then(array.out())
                .isEqualTo("[(-3: 3.0) (-2: 1.0) (0: 5.0) ]->[(3: 0.0) (9: 8.0) (15: 0.0) ]->[(19: 5.0) (21: 5.0) (34: -9.0) ]->[(109: 5.0) ]");
        then(array.values())
                .isEqualTo(new double[]{3D, 1D, 5D, 0D, 8D, 0D, 5D, 5D, -9D, 5D});
        then(array.size())
                .isEqualTo(10);

        array.trim(-2, 20);

        then(array.out())
                .isEqualTo("[(-2: 1.0) (0: 5.0) ]->[(3: 0.0) (9: 8.0) (15: 0.0) ]->[(19: 5.0) ]");
        then(array.values())
                .isEqualTo(new double[]{1D, 5D, 0D, 8D, 0D, 5D});
        then(array.size())
                .isEqualTo(6);

    }
}
