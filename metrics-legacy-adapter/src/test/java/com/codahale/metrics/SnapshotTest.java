package com.codahale.metrics;

import io.dropwizard.metrics5.UniformSnapshot;
import org.assertj.core.data.Offset;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class SnapshotTest {

    @Test
    public void testCreateSnapshot() throws Exception {
        Snapshot snapshot = Snapshot.of(new UniformSnapshot(new long[]{5, 1, 2, 3, 4}));

        assertThat(snapshot.getValues()).isEqualTo(new long[]{1, 2, 3, 4, 5});
        assertThat(snapshot.size()).isEqualTo(5);
        assertThat(snapshot.getMin()).isEqualTo(1);
        assertThat(snapshot.getMax()).isEqualTo(5);
        assertThat(snapshot.getStdDev()).isEqualTo(1.58, Offset.offset(0.01));
        assertThat(snapshot.getMedian()).isEqualTo(3, Offset.offset(0.01));
        assertThat(snapshot.get75thPercentile()).isEqualTo(4.5, Offset.offset(0.01));
        assertThat(snapshot.get95thPercentile()).isEqualTo(5, Offset.offset(0.01));
        assertThat(snapshot.get98thPercentile()).isEqualTo(5, Offset.offset(0.01));
        assertThat(snapshot.get99thPercentile()).isEqualTo(5, Offset.offset(0.01));
        assertThat(snapshot.get999thPercentile()).isEqualTo(5, Offset.offset(0.01));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        snapshot.dump(baos);
        assertThat(baos.toString("UTF-8")).isEqualToNormalizingNewlines("1\n" +
                "2\n" +
                "3\n" +
                "4\n" +
                "5\n");
    }
}
