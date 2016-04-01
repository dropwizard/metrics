package com.codahale.metrics.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.joda.time.Duration;
import com.papertrail.profiler.CpuProfile;

/**
 * An HTTP servlets which outputs a <a href="https://github.com/gperftools/gperftools">pprof</a> parseable response.
 */
public class CpuProfileServlet extends HttpServlet {
    private static final long serialVersionUID = -668666696530287501L;
    private static final String CONTENT_TYPE = "pprof/raw";
    private static final String CACHE_CONTROL = "Cache-Control";
    private static final String NO_CACHE = "must-revalidate,no-cache,no-store";
    private final Lock lock = new ReentrantLock();

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp) throws ServletException, IOException {

        int duration = 10;
        if (req.getParameter("duration") != null) {
            try {
                duration = Integer.parseInt(req.getParameter("duration"));
            } catch (NumberFormatException e) {
                duration = 10;
            }
        }

        int frequency = 100;
        if (req.getParameter("frequency") != null) {
            try {
                frequency = Integer.parseInt(req.getParameter("frequency"));
            } catch (NumberFormatException e) {
                frequency = 100;
            }
        }

        final Thread.State state;
        if ("blocked".equalsIgnoreCase(req.getParameter("state"))) {
            state = Thread.State.BLOCKED;
        }
        else {
            state = Thread.State.RUNNABLE;
        }

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setHeader(CACHE_CONTROL, NO_CACHE);
        resp.setContentType(CONTENT_TYPE);
        final OutputStream output = resp.getOutputStream();
        try {
            doProfile(output, duration, frequency, state);
        } finally {
            output.close();
        }
    }

    protected void doProfile(OutputStream out, int duration, int frequency, Thread.State state) throws IOException {
        if (lock.tryLock()) {
            try {
                CpuProfile profile = CpuProfile.record(Duration.standardSeconds(duration),
                        frequency, state);
                if (profile == null) {
                    throw new RuntimeException("could not create CpuProfile");
                }
                profile.writeGoogleProfile(out);
                return;
            } finally {
                lock.unlock();
            }
        }
        throw new RuntimeException("Only one profile request may be active at a time");
    }
}
