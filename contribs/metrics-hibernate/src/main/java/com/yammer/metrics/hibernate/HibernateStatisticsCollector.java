package com.yammer.metrics.hibernate;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Gauge;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;

public class HibernateStatisticsCollector {

    public static void register(SessionFactory sessionFactory) {
        register(sessionFactory.getStatistics());
    }

    /**
     * Instruments the given {@link org.hibernate.stat.Statistics} instance with a set of gauges
     * for Hibernate's built-in statistics:
     * <p/>
     * <table>
     * <tr>
     * <td>{@code connection-count}</td>
     * <td>The number of connection requests.</td>
     * </tr>
     * <tr>
     * <td>{@code flush-count}</td>
     * <td>Number of flushes done on the session (either by client code or by hibernate).</td>
     * </tr>
     * <tr>
     * <td>{@code transaction-count}</td>
     * <td>The number of completed transactions (failed and successful).</td>
     * </tr>
     * <tr>
     * <td>{@code transaction-count-successful}</td>
     * <td>The number of transactions completed without failure</td>
     * </tr>
     * <tr>
     * <td>{@code session-open-count}</td>
     * <td>The number of sessions your code has opened.</td>
     * </tr>
     * <tr>
     * <td>{@code session-close-count}</td>
     * <td>The number of sessions your code has closed.</td>
     * </tr>
     * <tr>
     * <td>{@code query-execution-count}</td>
     * <td>Total number of queries executed.</td>
     * </tr>
     * <tr>
     * <td>{@code query-execution-max-time}</td>
     * <td>Time of the slowest query executed.</td>
     * </tr>
     * </table>
     *
     * @param statistics an {@link Statistics} instance
     * @see Statistics
     */
    public static void register(final Statistics statistics) {

        statistics.setStatisticsEnabled(true);

        // Number of connection requests. Note that this number represents
        // the number of times Hibernate asked for a connection, and
        // NOT the number of connections (which is determined by your
        // pooling mechanism).
        Metrics.newGauge(Statistics.class, "connection-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getConnectCount();
            }
        });

        // Number of flushes done on the session (either by client code or by hibernate).
        Metrics.newGauge(Statistics.class, "flush-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getFlushCount();
            }
        });

        // The number of completed transactions (failed and successful).
        Metrics.newGauge(Statistics.class, "transaction-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getTransactionCount();
            }
        });

        // The number of transactions completed without failure
        Metrics.newGauge(Statistics.class, "transaction-count-successful", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getSuccessfulTransactionCount();
            }
        });

        // The number of sessions your code has opened.
        Metrics.newGauge(Statistics.class, "session-open-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getSessionOpenCount();
            }
        });

        // The number of sessions your code has closed.
        Metrics.newGauge(Statistics.class, "session-close-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getSessionCloseCount();
            }
        });

        // Total number of queries executed.
        Metrics.newGauge(Statistics.class, "query-execution-count", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getQueryExecutionCount();
            }
        });

        // Time of the slowest query executed.
        Metrics.newGauge(Statistics.class, "query-execution-max-time", new Gauge<Long>() {
            @Override
            public Long getValue() {
                return statistics.getQueryExecutionMaxTime();
            }
        });

    }

}
