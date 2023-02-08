package com.codahale.metrics;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("deprecation")
class FixedNameCsvFileProviderTest {

    private Path tempDirectory;
    private FixedNameCsvFileProvider fixedNameCsvFileProvider = new FixedNameCsvFileProvider();

    @BeforeEach
    void setUp() throws Exception {
        tempDirectory = Files.createTempDirectory("csv-test");
    }

    @AfterEach
    void tearDown() throws Exception {
        Files.delete(tempDirectory);
    }

    @Test
    void getFile() throws Exception {
        File file = fixedNameCsvFileProvider.getFile(tempDirectory.toFile(), "timer-test");
        assertThat(file.toString()).startsWith(tempDirectory.toString());
        assertThat(file.toString()).endsWith("timer-test.csv");
    }
}
