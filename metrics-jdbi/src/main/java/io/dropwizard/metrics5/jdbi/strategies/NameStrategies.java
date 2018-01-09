package io.dropwizard.metrics5.jdbi.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import org.skife.jdbi.v2.ClasspathStatementLocator;
import org.skife.jdbi.v2.StatementContext;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class NameStrategies {
    public static final StatementNameStrategy CHECK_EMPTY = new CheckEmptyStrategy();
    public static final StatementNameStrategy CHECK_RAW = new CheckRawStrategy();
    public static final StatementNameStrategy SQL_OBJECT = new SqlObjectStrategy();
    public static final StatementNameStrategy NAIVE_NAME = new NaiveNameStrategy();
    public static final StatementNameStrategy CONTEXT_CLASS = new ContextClassStrategy();
    public static final StatementNameStrategy CONTEXT_NAME = new ContextNameStrategy();

    /**
     * An empty SQL statement.
     */
    private static final MetricName EMPTY_SQL = MetricName.build("sql.empty");

    /**
     * Unknown SQL.
     */
    static final MetricName UNKNOWN_SQL = MetricName.build("sql.unknown");

    /**
     * Context attribute name for the metric class.
     */
    public static final String STATEMENT_CLASS = "_metric_class";

    /**
     * Context attribute name for the metric group.
     */
    public static final String STATEMENT_GROUP = "_metric_group";

    /**
     * Context attribute name for the metric type.
     */
    public static final String STATEMENT_TYPE = "_metric_type";

    /**
     * Context attribute name for the metric name.
     */
    public static final String STATEMENT_NAME = "_metric_name";

    private static MetricName forRawSql(String rawSql) {
        return MetricRegistry.name("sql", "raw", rawSql);
    }

    static final class CheckEmptyStrategy implements StatementNameStrategy {
        private CheckEmptyStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final String rawSql = statementContext.getRawSql();

            if (rawSql == null || rawSql.length() == 0) {
                return EMPTY_SQL;
            }
            return null;
        }
    }

    static final class CheckRawStrategy implements StatementNameStrategy {
        private CheckRawStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final String rawSql = statementContext.getRawSql();

            if (ClasspathStatementLocator.looksLikeSql(rawSql)) {
                return forRawSql(rawSql);
            }
            return null;
        }
    }

    static final class NaiveNameStrategy implements StatementNameStrategy {
        private NaiveNameStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final String rawSql = statementContext.getRawSql();

            // Is it using the template loader?
            final int colon = rawSql.indexOf(':');

            if (colon == -1) {
                // No package? Just return the name, JDBI figured out somehow on how to find the raw sql for this statement.
                return forRawSql(rawSql);
            }

            final String group = rawSql.substring(0, colon);
            final String name = rawSql.substring(colon + 1);
            return MetricRegistry.name(group, name);
        }
    }

    static final class SqlObjectStrategy implements StatementNameStrategy {
        private SqlObjectStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final Class<?> clazz = statementContext.getSqlObjectType();
            final Method method = statementContext.getSqlObjectMethod();
            if (clazz != null) {
                final String rawSql = statementContext.getRawSql();

                final String group = clazz.getPackage().getName();
                final String name = clazz.getSimpleName();
                final String type = method == null ? rawSql : method.getName();
                return MetricRegistry.name(group, name, type);
            }
            return null;
        }
    }

    static final class ContextClassStrategy implements StatementNameStrategy {
        private ContextClassStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final Object classObj = statementContext.getAttribute(STATEMENT_CLASS);
            final Object nameObj = statementContext.getAttribute(STATEMENT_NAME);

            if (classObj == null || nameObj == null) {
                return null;
            }

            final String className = (String) classObj;
            final String statementName = (String) nameObj;

            final int dotPos = className.lastIndexOf('.');
            if (dotPos == -1) {
                return null;
            }

            return MetricRegistry.name(className.substring(0, dotPos),
                    className.substring(dotPos + 1),
                    statementName);
        }
    }

    static final class ContextNameStrategy implements StatementNameStrategy {
        /**
         * File pattern to shorten the group name.
         */
        private static final Pattern SHORT_PATTERN = Pattern.compile("^(.*?)/(.*?)(-sql)?\\.st(g)?$");

        private ContextNameStrategy() {
        }

        @Override
        public MetricName getStatementName(StatementContext statementContext) {
            final Object groupObj = statementContext.getAttribute(STATEMENT_GROUP);
            final Object typeObj = statementContext.getAttribute(STATEMENT_TYPE);
            final Object nameObj = statementContext.getAttribute(STATEMENT_NAME);

            if (groupObj == null || nameObj == null) {
                return null;
            }

            final String group = (String) groupObj;
            final String statementName = (String) nameObj;

            if (typeObj == null) {
                final Matcher matcher = SHORT_PATTERN.matcher(group);
                if (matcher.matches()) {
                    final String groupName = matcher.group(1);
                    final String typeName = matcher.group(2);
                    return MetricRegistry.name(groupName, typeName, statementName);
                }

                return MetricRegistry.name(group, statementName, "");
            } else {
                final String type = (String) typeObj;

                return MetricRegistry.name(group, type, statementName);
            }
        }
    }

    private NameStrategies() {
    }
}
