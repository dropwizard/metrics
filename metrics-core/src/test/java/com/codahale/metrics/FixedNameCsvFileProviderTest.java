package com.codahale.metrics;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedNameCsvFileProviderTest {
    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private File dataDirectory;

    @Before
    public void setUp() throws Exception {
        this.dataDirectory = folder.newFolder();
    }

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