package com.codahale.metrics.health;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class SharedMetricRegistriesTest {
    @Before
    public void setUp() throws Exception {
        SharedHealthCheckRegistries.clear();
    }

    @Test
    public void memoizesRegistriesByName() throws Exception {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("one");
        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("one");

        assertThat(one)
                .isSameAs(two);
    }

    @Test
    public void hasASetOfNames() throws Exception {
        SharedHealthCheckRegistries.getOrCreate("one");

        assertThat(SharedHealthCheckRegistries.names())
                .containsOnly("one");
    }

    @Test
    public void removesRegistries() throws Exception {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("one");
        SharedHealthCheckRegistries.remove("one");

        assertThat(SharedHealthCheckRegistries.names())
                .isEmpty();

        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("one");
        assertThat(two)
                .isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() throws Exception {
        SharedHealthCheckRegistries.getOrCreate("one");
        SharedHealthCheckRegistries.getOrCreate("two");

        SharedHealthCheckRegistries.clear();

        assertThat(SharedHealthCheckRegistries.names())
                .isEmpty();
    }
}
