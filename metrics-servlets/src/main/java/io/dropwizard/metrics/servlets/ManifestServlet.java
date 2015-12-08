package io.dropwizard.metrics.servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An HTTP servlet which outputs a {@code text/plain} {@code "META-INF/MANIFEST.MF"} as a response.
 * Manifest will be read and cached on the first request
 * 
 */
public class ManifestServlet extends HttpServlet {
	
    private static final long serialVersionUID = 3772654177231086757L;

    private static final String CONTENT_TYPE = "text/plain";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "must-revalidate,no-cache,no-store";

    private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";

    private static volatile String manifestPlain = null; 

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader(CACHE_CONTROL, NO_CACHE);
        resp.setContentType(CONTENT_TYPE);
        final PrintWriter writer = resp.getWriter();
        try {
            writer.println(getManifestPlain());
        } finally {
            writer.close();
        }
    }

    /**
     * Get manifest as Plain Text. 
     * Will return cached version if manifest is in cache already,
     * or will read manifest from classpath and cache it
     * 
     * @return
     * @throws IOException
     */
    private String getManifestPlain() throws IOException {
    	if (manifestPlain == null) {
    		synchronized (this) {
    			if (manifestPlain == null) {
    		    	ServletContext app = getServletConfig().getServletContext();
    		    	InputStream in = app.getResourceAsStream(MANIFEST_FILE);

    		    	StringBuffer sb = new StringBuffer();
    		    	if (in != null) {
    		    		List<String> lines = readLines(in);
    		    		for (String line : lines) {
    		    			sb.append(line).append("\n");
    		    		}
    		    	} else {
    		    		sb.append("Not found: " + MANIFEST_FILE);
    		    	}
    				manifestPlain = sb.toString();
    			}
    		}
    	}
    	return manifestPlain;
    }

    /**
     * Reads stream by lines
     * 
     * @param in
     * @return
     * @throws IOException
     */
    private List<String> readLines(InputStream in) throws IOException {
    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
    	List<String> result = new ArrayList<>();
    	String strLine;
    	while ((strLine = br.readLine()) != null) {
    		result.add(strLine);
    	}
    	br.close();

    	return result;
    }

}
