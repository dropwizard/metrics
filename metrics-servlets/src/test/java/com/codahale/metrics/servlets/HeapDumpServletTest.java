package com.codahale.metrics.servlets;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class HeapDumpServletTest extends AbstractServletTest {
    @Rule
    public final TemporaryFolder temporaryFolder = TemporaryFolder.builder()
            .assureDeletion()
            .build();

    @Override
    protected void setUp(ServletTester tester) {
        tester.addServlet(HeapDumpServlet.class, "/heapdump");
    }

    @Before
    public void setUp() throws Exception {
        request.setMethod("POST");
        request.setVersion("HTTP/1.0");
    }

    @Test
    public void defaultWithoutAnyQueryParametersSucceeds() throws Exception {
        request.setURI("/heapdump");
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith("text/plain");
        assertThat(response.getContent()).startsWith("Heap dump successfully written to ");
    }

    @Test
    public void outputFileOverridesOutputFilePath() throws Exception {
        final File tempFile = new File(temporaryFolder.getRoot(), "heapdump.hprof");
        request.setURI("/heapdump?outputFile=" + tempFile);
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith("text/plain");
        assertThat(response.getContent()).isEqualToIgnoringNewLines("Heap dump successfully written to " + tempFile.getCanonicalPath());
        assertThat(tempFile)
                .exists()
                .isReadable()
                .isNotEmpty();
    }

    @Test
    public void downloadParameterStreamsFile() throws Exception {
        request.setURI("/heapdump?download=true");
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/octet-stream");
        final int contentLength = response.getField(HttpHeader.CONTENT_LENGTH).getIntValue();
        assertThat(contentLength).isPositive();
        assertThat(response.getContentBytes()).hasSize(contentLength);
    }

    @Test
    public void deleteAfterDownloadDefaultValueIsTrue() throws Exception {
        final File tempFile = new File(temporaryFolder.getRoot(), "heapdump.hprof");
        request.setURI("/heapdump?outputFile=" + tempFile + "&download=true");
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/octet-stream");
        final int contentLength = response.getField(HttpHeader.CONTENT_LENGTH).getIntValue();
        assertThat(contentLength).isPositive();
        assertThat(response.getContentBytes()).hasSize(contentLength);
        assertThat(tempFile).doesNotExist();
    }

    @Test
    @Ignore
    public void deleteAfterDownloadFalseKeepsHeapDumpFile() throws Exception {
        final File tempFile = new File(temporaryFolder.getRoot(), "heapdump.hprof");
        request.setURI("/heapdump?outputFile=" + tempFile + "&download=true&deleteAfterDownload=false");
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).isEqualTo("application/octet-stream");
        final int contentLength = response.getField(HttpHeader.CONTENT_LENGTH).getIntValue();
        assertThat(contentLength).isPositive();
        assertThat(response.getContentBytes()).hasSize(contentLength);
        assertThat(tempFile)
                .exists()
                .isReadable()
                .isNotEmpty();
    }

    @Test
    public void deleteAfterDownloadWithoutDownloadKeepsHeapDumpFile() throws Exception {
        final File tempFile = new File(temporaryFolder.getRoot(), "heapdump.hprof");
        request.setURI("/heapdump?outputFile=" + tempFile + "&download=false&deleteAfterDownload=true");
        processRequest();

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith("text/plain");
        assertThat(response.getContent()).isEqualToIgnoringNewLines("Heap dump successfully written to " + tempFile.getCanonicalPath());
        assertThat(tempFile)
                .exists()
                .isReadable()
                .isNotEmpty();
    }

    @Test
    public void returnsNotCacheable() throws Exception {
        request.setURI("/heapdump");
        processRequest();
        assertThat(response.get(HttpHeader.CACHE_CONTROL)).isEqualTo("must-revalidate,no-cache,no-store");
    }

    @Test
    public void outputFileMustHaveExtensionHProf() throws Exception {
        request.setURI("/heapdump?outputFile=test.txt");
        processRequest();
        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.get(HttpHeader.CACHE_CONTROL)).isEqualTo("must-revalidate,no-cache,no-store");
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith("text/plain");
        assertThat(response.getContent()).isEqualToIgnoringNewLines("An error occurred when trying to take a heap dump: heapdump file must have .hprof extention");
    }

    @Test
    public void outputFileMustNotExist() throws Exception {
        final File tempFile = temporaryFolder.newFile("heapdump.hprof");
        request.setURI("/heapdump?outputFile=" + tempFile);
        processRequest();

        assertThat(response.getStatus()).isEqualTo(500);
        assertThat(response.get(HttpHeader.CACHE_CONTROL)).isEqualTo("must-revalidate,no-cache,no-store");
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith("text/plain");
        assertThat(response.getContent()).isEqualToIgnoringNewLines("An error occurred when trying to take a heap dump: File exists");
    }
}
