package io.dropwizard.metrics5;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class FixedNameCsvFileProviderTest {
    @TempDir
    public File folder;

    private File dataDirectory;

    @BeforeEach
    void setUp() throws Exception {
        this.dataDirectory = newFolder(folder, "junit");
    }

    @Test
    void testGetFile() {
        FixedNameCsvFileProvider provider = new FixedNameCsvFileProvider();
        File file = provider.getFile(dataDirectory, "test");
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("test.csv");
    }

    @Test
    void testGetFileSanitize() {
        FixedNameCsvFileProvider provider = new FixedNameCsvFileProvider();
        File file = provider.getFile(dataDirectory, "/myfake/uri");
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("myfake.uri.csv");
    }

    private static File newFolder(File root, String... subDirs) throws IOException {
        String subFolder = String.join("/", subDirs);
        File result = new File(root, subFolder);
        if (!result.mkdirs()) {
            throw new IOException("Couldn't create folders " + root);
        }
        return result;
    }
}