package com.codahale.metrics.graphite;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.regex.Pattern;
import java.nio.charset.Charset;

/**
 * Created by p.willoughby on 27/11/14.
 */
public class GraphitePickler {
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
    /**
     * Minimally necessary pickle opcodes.
     */
    private static final char
            MARK = '(',
            STOP = '.',
            LONG = 'L',
            STRING = 'S',
            APPEND = 'a',
            LIST = 'l',
            TUPLE = 't',
            QUOTE = '\'',
            LF = '\n';

    /**
     * See: http://readthedocs.org/docs/graphite/en/1.0/feeding-carbon.html
     *
     * @throws java.io.IOException
     */
    static byte[] pickleMetrics(List<MetricTuple> metrics, Charset charset) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(metrics.size() * 75); // Extremely rough estimate of 75 bytes per message
        Writer pickled = new OutputStreamWriter(out, charset);

        pickled.append(MARK);
        pickled.append(LIST);

        for (MetricTuple tuple : metrics) {
            // start the outer tuple
            pickled.append(MARK);

            // the metric name is a string.
            pickled.append(STRING);
            // the single quotes are to match python's repr("abcd")
            pickled.append(QUOTE);
            pickled.append(sanitize(tuple.name));
            pickled.append(QUOTE);
            pickled.append(LF);

            // start the inner tuple
            pickled.append(MARK);

            // timestamp is a long
            pickled.append(LONG);
            pickled.append(Long.toString(tuple.timestamp));
            // the trailing L is to match python's repr(long(1234))
            pickled.append(LONG);
            pickled.append(LF);

            // and the value is a string.
            pickled.append(STRING);
            pickled.append(QUOTE);
            pickled.append(sanitize(tuple.value));
            pickled.append(QUOTE);
            pickled.append(LF);

            pickled.append(TUPLE); // inner close
            pickled.append(TUPLE); // outer close

            pickled.append(APPEND);
        }

        // every pickle ends with STOP
        pickled.append(STOP);

        pickled.flush();

        return out.toByteArray();
    }

    public static class MetricTuple {
        String name;
        long timestamp;
        String value;

        MetricTuple(String name, long timestamp, String value) {
            this.name = name;
            this.timestamp = timestamp;
            this.value = value;
        }
    }

    protected static String sanitize(String s) {
        return WHITESPACE.matcher(s).replaceAll("-");
    }
}
