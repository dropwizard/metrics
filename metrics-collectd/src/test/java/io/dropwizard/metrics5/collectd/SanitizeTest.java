package io.dropwizard.metrics5.collectd;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SanitizeTest {

    private Sanitize sanitize = new Sanitize(Sanitize.DEFAULT_MAX_LENGTH);

    @Test
    void replacesIllegalCharactersInName() throws Exception {
        assertThat(sanitize.name("foo\u0000bar/baz-quux")).isEqualTo("foo_bar_baz_quux");
    }

    @Test
    void replacesIllegalCharactersInInstanceName() throws Exception {
        assertThat(sanitize.instanceName("foo\u0000bar/baz-quux")).isEqualTo("foo_bar_baz-quux");
    }

    @Test
    void truncatesNamesExceedingMaxLength() throws Exception {
        String longName = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";
        assertThat(sanitize.name(longName)).isEqualTo(longName.substring(0, (Sanitize.DEFAULT_MAX_LENGTH)));
    }

    @Test
    void truncatesNamesExceedingCustomMaxLength() throws Exception {
        Sanitize customSanitize = new Sanitize(70);
        String longName = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";
        assertThat(customSanitize.name(longName)).isEqualTo(longName.substring(0, 70));
    }

    @Test
    void replacesNonASCIICharacters() throws Exception {
        assertThat(sanitize.name("M" + '\u00FC' + "nchen")).isEqualTo("M_nchen");
    }

}
