package io.dropwizard.metrics5.collectd;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SanitizeTest {

    @Test
    public void replacesIllegalCharactersInName() throws Exception {
        assertThat(Sanitize.name("foo\u0000bar/baz-quux")).isEqualTo("foo_bar_baz_quux");
    }

    @Test
    public void replacesIllegalCharactersInInstanceName() throws Exception {
        assertThat(Sanitize.instanceName("foo\u0000bar/baz-quux")).isEqualTo("foo_bar_baz-quux");
    }

    @Test
    public void truncatesNamesExceedingMaxLength() throws Exception {
        String longName = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";
        assertThat(Sanitize.name(longName)).isEqualTo(longName.substring(0, 63));
    }

    @Test
    public void replacesNonASCIICharacters() throws Exception {
        assertThat(Sanitize.name("M" + '\u00FC' + "nchen")).isEqualTo("M_nchen");
    }

}
