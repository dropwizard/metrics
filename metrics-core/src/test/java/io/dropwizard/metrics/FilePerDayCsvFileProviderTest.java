package io.dropwizard.metrics;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.CharBuffer;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FilePerDayCsvFileProviderTest
{
    private static final int ONE_DAY = 1000 * 60 * 60 * 24;

    @Rule
    public final TemporaryFolder folder = new TemporaryFolder();

    private final MetricRegistry registry = mock(MetricRegistry.class);
    private final Clock clock = mock(Clock.class);

    private File dataDirectory;

    @Before
    public void setUp() throws Exception
    {
        this.dataDirectory = folder.newFolder();
    }

    @Test
    public void testGetFile() {
        FilePerDayCsvFileProvider provider = new FilePerDayCsvFileProvider(clock, 10);

        when(clock.getTime()).thenReturn(1446194864095L);
        File file = provider.getFile(dataDirectory, MetricName.build( "test" ));
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("test_20151030.csv");

        when(clock.getTime()).thenReturn(1446194864095L + ONE_DAY);
        file = provider.getFile(dataDirectory, MetricName.build( "test" ));
        assertThat(file.getParentFile()).isEqualTo(dataDirectory);
        assertThat(file.getName()).isEqualTo("test_20151031.csv");
    }

    @Test
    public void oldestFilesShouldBeCleanedUp() throws IOException
    {

        FilePerDayCsvFileProvider fileProvider = new FilePerDayCsvFileProvider(clock, 3);
        CsvReporter reporter = CsvReporter.forRegistry( registry )
                .formatFor( Locale.US)
                .convertRatesTo( TimeUnit.SECONDS)
                .convertDurationsTo( TimeUnit.MILLISECONDS)
                .withClock(clock)
                .filter( MetricFilter.ALL)
                .withCsvFileProvider(fileProvider)
                .build(dataDirectory);


        when(clock.getTime()).thenReturn(1446194864095L);

        final Gauge gauge = mock(Gauge.class);
        when(gauge.getValue()).thenReturn(1);

        reporter.report(map("gauge", gauge),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        assertThat(fileContents("gauge_20151030.csv"))
                .isEqualTo(csv(
                        "t,value",
                        "1446194864,1"
                ));

        when(clock.getTime()).thenReturn(1446194864095L + ONE_DAY);
        when(gauge.getValue()).thenReturn(2);

        reporter.report(map("gauge", gauge),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());
        assertThat(fileContents("gauge_20151030.csv"))
                .isEqualTo(csv(
                        "t,value",
                        "1446194864,1"
                ));
        assertThat(fileContents("gauge_20151031.csv"))
                .isEqualTo(csv(
                        "t,value",
                        "1446281264,2"
                ));

        when(clock.getTime()).thenReturn(1446194864095L + (ONE_DAY*3));
        when(gauge.getValue()).thenReturn(3);

        reporter.report(map("gauge", gauge),
                this.<Counter>map(),
                this.<Histogram>map(),
                this.<Meter>map(),
                this.<Timer>map());

        assertThat( new File( dataDirectory, "gauge_20151030.csv") ).doesNotExist();
        assertThat( new File( dataDirectory, "gauge_20151031.csv") ).exists();
        assertThat(fileContents("gauge_20151102.csv"))
                .isEqualTo(csv(
                        "t,value",
                        "1446454064,3"
                ));

    }

    private String csv(String... lines) {
        final StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line).append( String.format( "%n" ));
        }
        return builder.toString();
    }

    private String fileContents(String filename) throws IOException
    {
        final StringBuilder builder = new StringBuilder();
        final FileInputStream input = new FileInputStream(new File(dataDirectory, filename));
        try {
            final InputStreamReader reader = new InputStreamReader(input);
            final BufferedReader bufferedReader = new BufferedReader(reader);
            final CharBuffer buf = CharBuffer.allocate( 1024 );
            while (bufferedReader.read(buf) != -1) {
                buf.flip();
                builder.append(buf);
                buf.clear();
            }
        } finally {
            input.close();
        }
        return builder.toString();
    }

    private <T> SortedMap<MetricName, T> map() {
        return new TreeMap<MetricName, T>();
    }

    private <T> SortedMap<MetricName, T> map(String name, T metric) {
        final TreeMap<MetricName, T> map = new TreeMap<MetricName, T>();
        map.put( MetricName.build( name ), metric);
        return map;
    }

}