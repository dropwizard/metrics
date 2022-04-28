package io.dropwizard.metrics.servlets;

import com.sun.management.HotSpotDiagnosticMXBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import javax.management.MBeanServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * * @param  outputFile the system-dependent filename
 * * @param  live if {@code true} dump only <i>live</i> objects
 * *         i.e. objects that are reachable from others
 * * @param  Download the created heap dump
 */
public class HeapDumpServlet extends HttpServlet {
    static final String[] SUPPORTED_VMS = {"OpenJDK", "HotSpot", "GraalVM"};

    private static final long serialVersionUID = 1L;
    private static final String CONTENT_TYPE_TEXT_PLAIN = "text/plain";
    private static final String CONTENT_TYPE_OCTET_STREAM = "application/octet-stream";

    private final String vmName;

    public HeapDumpServlet() {
        this(System.getProperty("java.vm.name"));
    }

    private HeapDumpServlet(String vmName) {
        this.vmName = requireNonNull(vmName, "vmName");
    }

    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setHeader("Cache-Control", "must-revalidate,no-cache,no-store");

        if (!isJvmSupported(vmName)) {
            resp.setContentType(CONTENT_TYPE_TEXT_PLAIN);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("Sorry this JVM does not allow to take heap dumps: " + vmName);
            return;
        }

        final String outputFileParam = req.getParameter("outputFile");
        final boolean live = getBooleanParam(req.getParameter("live"), false);
        final boolean download = getBooleanParam(req.getParameter("download"), false);
        final boolean deleteAfterDownload = getBooleanParam(req.getParameter("deleteAfterDownload"), true);

        final String outputFile;
        if (outputFileParam == null) {
            final Path tempDirectory = Files.createTempDirectory("heap-dump");
            outputFile = tempDirectory.resolve("heapdump.hprof").normalize().toString();
        } else {
            outputFile = outputFileParam;
        }

        final Optional<Exception> ex = dumpHeap(outputFile, live);
        if (ex.isPresent()) {
            Exception exception = ex.get();
            resp.setContentType(CONTENT_TYPE_TEXT_PLAIN);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("An error occurred when trying to take a heap dump: " + exception.getMessage());
            return;
        }

        final Path heapDumpPath = Paths.get(outputFile);
        final File heapDumpFile = heapDumpPath.toFile();
        if (download) {
            if (heapDumpFile.isFile() && heapDumpFile.canRead()) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType(CONTENT_TYPE_OCTET_STREAM);
                resp.setHeader("Content-Length", Long.toString(Files.size(heapDumpPath)));
                try (FileInputStream fileInputStream = new FileInputStream(heapDumpFile);
                     OutputStream output = resp.getOutputStream()) {
                    copyStream(fileInputStream, output);

                } catch (Exception e) {
                    resp.setContentType(CONTENT_TYPE_TEXT_PLAIN);
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().println("An error occurred when trying to send the heap dump: " + e.getMessage());
                } finally {
                    if (deleteAfterDownload) {
                        heapDumpFile.delete();
                    }
                }
            } else {
                resp.setContentType(CONTENT_TYPE_TEXT_PLAIN);
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().println("An error occurred when trying to read the heap dump at " + heapDumpFile.getCanonicalPath());
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENT_TYPE_TEXT_PLAIN);
            resp.getWriter().println("Heap dump successfully written to " + heapDumpFile.getCanonicalPath());
        }
    }

    private Boolean getBooleanParam(String param, boolean defaultValue) {
        return param == null ? defaultValue : Boolean.parseBoolean(param);
    }

    private boolean isJvmSupported(String vmName) {
        for (String supportedVm : SUPPORTED_VMS) {
            if (vmName.contains(supportedVm)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param outputFile the system-dependent filename
     * @param live       if {@code true} dump only <i>live</i> objects i.e. objects that are reachable from others
     * @return An optional exception if taking the heap dump failed.
     */
    Optional<Exception> dumpHeap(String outputFile, boolean live) {
        try {
            // Some PaaS like Google App Engine blacklist java.lang.managament
            final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            final HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            mxBean.dumpHeap(outputFile, live);
        } catch (Exception e) {
            return Optional.of(e);
        }

        return Optional.empty();
    }

    private void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int length;
        while ((length = in.read(buffer)) != -1) {
            out.write(buffer, 0, length);
        }
    }
}