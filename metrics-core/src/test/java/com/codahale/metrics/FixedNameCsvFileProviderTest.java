package com.codahale.metrics;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedNameCsvFileProviderTest {

    @TempDir
    File dataDirectory;

    @Test
    public void testGetFile() {
        FixedNameCsvFileProvider provider = new FixedNameCsvFileProvider();
        File file = provider.getFile(dataDirectory, "test");
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("test.csv");
    }

    @Test
    public void testGetFileSanitize() {
        FixedNameCsvFileProvider provider = new FixedNameCsvFileProvider();
        File file = provider.getFile(dataDirectory, "/myfake/uri");
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("myfake.uri.csv");
    }
}