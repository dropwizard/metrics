package io.dropwizard.metrics.health;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SharedHealthCheckRegistriesTest {
    @Before
    public void setUp() throws Exception {
        SharedHealthCheckRegistries.clear();
    }

    @Test
    public void memoizesRegistriesByName() throws Exception {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("one");
        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("one");

        assertThat(one).isSameAs(two);
    }

    @Test
    public void hasASetOfNames() throws Exception {
        SharedHealthCheckRegistries.getOrCreate("one");

        assertThat(SharedHealthCheckRegistries.names()).containsOnly("one");
    }

    @Test
    public void removesRegistries() throws Exception {
        final HealthCheckRegistry one = SharedHealthCheckRegistries.getOrCreate("one");
        SharedHealthCheckRegistries.remove("one");

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();

        final HealthCheckRegistry two = SharedHealthCheckRegistries.getOrCreate("one");
        assertThat(two).isNotSameAs(one);
    }

    @Test
    public void clearsRegistries() throws Exception {
        SharedHealthCheckRegistries.getOrCreate("one");
        SharedHealthCheckRegistries.getOrCreate("two");

        SharedHealthCheckRegistries.clear();

        assertThat(SharedHealthCheckRegistries.names()).isEmpty();
    }

    @Test
    public void defaultRegistry() throws Exception {
        // Verify that an error is thrown when default is not set
        try {
            SharedHealthCheckRegistries.getDefault();
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry has not been set.");
        }
        
        // Verify that default can be set
        final HealthCheckRegistry registry = new HealthCheckRegistry();
        SharedHealthCheckRegistries.setDefault(registry);
        assertThat(SharedHealthCheckRegistries.getDefault()).isEqualTo(registry);
        
        // Verify that default can not be set again
        try {
            SharedHealthCheckRegistries.setDefault(new HealthCheckRegistry());
        } catch (final Exception e) {
            assertThat(e).isInstanceOf(IllegalStateException.class);
            assertThat(e.getMessage()).isEqualTo("Default registry has already been set.");
        }
    }
}
