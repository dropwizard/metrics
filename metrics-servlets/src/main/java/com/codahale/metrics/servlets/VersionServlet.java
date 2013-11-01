package com.codahale.metrics.servlets;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.List;

import static java.lang.String.format;

public class VersionServlet extends AppDiagnosticBaseServlet {

    private static final long serialVersionUID = -2037820542366252085L;

    private static final String DEFAULT_CONTENT = "Version.txt not found";
    private transient String version_content;
    private transient Logger LOGGER = LoggerFactory.getLogger(VersionServlet.class);

    @Override
    public void init(ServletConfig config) throws ServletException {
        version_content = DEFAULT_CONTENT;
        InputStream resourceAsStream = null;
        try {
            resourceAsStream = getClass().getClassLoader().getResourceAsStream("version.txt");
            if (resourceAsStream != null) {
                List<String> lines = IOUtils.readLines(resourceAsStream);
                StringBuilder versionFileLines = new StringBuilder();
                for (String line : lines) {
                    versionFileLines.append(line).append(format("%n"));
                }
                version_content = versionFileLines.toString();
            } else {
                LOGGER.warn("Problem encountered while version requested");
            }
        } catch (Exception e) {
            LOGGER.warn("Problem encountered while version requested", e);
        } finally {
            IOUtils.closeQuietly(resourceAsStream);
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        PrintWriter writer = null;
        try {
            writer = resp.getWriter();
            writer.print(version_content);
            writer.flush();
        } catch (Exception e) {
            LOGGER.warn("Problem encountered while version requested", e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @Override
    public String uri() {
        return "/version";
    }

    @Override
    public String displayName() {
        return "Version";
    }

    @Override
    public boolean supportsPrettyPrint() {
        return false;
    }
}