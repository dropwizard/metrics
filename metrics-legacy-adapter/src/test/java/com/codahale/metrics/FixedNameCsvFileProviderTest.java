package com.codahale.metrics;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
public class FixedNameCsvFileProviderTest {

    private Path tempDirectory;
    private FixedNameCsvFileProvider fixedNameCsvFileProvider = new FixedNameCsvFileProvider();

    @Before
    public void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("csv-test");
    }

    @After
    public void tearDown() throws Exception {
        Files.delete(tempDirectory);
    }

    @Test
    public void getFile() throws Exception {
        File file = fixedNameCsvFileProvider.getFile(tempDirectory.toFile(), "timer-test");
        assertThat(file.toString()).startsWith(tempDirectory.toString());
        assertThat(file.toString()).endsWith("timer-test.csv");
    }
}
