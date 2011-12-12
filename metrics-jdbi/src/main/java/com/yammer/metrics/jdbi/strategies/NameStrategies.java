package com.yammer.metrics.jdbi.strategies;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.skife.jdbi.v2.ClasspathStatementLocator;
import org.skife.jdbi.v2.StatementContext;

import com.yammer.metrics.core.MetricName;

public final class NameStrategies
{
    public static final StatementNameStrategy CHECK_EMPTY = new CheckEmptyStrategy();
    public static final StatementNameStrategy CHECK_RAW = new CheckRawStrategy();
    public static final StatementNameStrategy SQL_OBJECT = new SqlObjectStrategy();
    public static final StatementNameStrategy NAIVE_NAME  = new NaiveNameStrategy();
    public static final StatementNameStrategy CONTEXT_CLASS  = new ContextClassStrategy();
    public static final StatementNameStrategy CONTEXT_NAME  = new ContextNameStrategy();

    /** An empty SQL statement. */
    private static final MetricName EMPTY_SQL = new MetricName("sql", "empty", "");

    /** Unknown SQL. */
    static final MetricName UNKNOWN_SQL = new MetricName("sql", "unknown", "");

    /** Context element for JMX class. */
    public static final String STATEMENT_CLASS = "_jmx_class";

    /** Context element for JMX group. */
    public static final String STATEMENT_GROUP = "_jmx_group";

    /** Context element for JMX name. */
    public static final String STATEMENT_NAME = "_jmx_name";

    private static MetricName forRawSql(final String rawSql)
    {
        return StatementName.getJmxSafeName("sql", "raw", rawSql);
    }

    static final class CheckEmptyStrategy implements StatementNameStrategy
    {
        private CheckEmptyStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
            final String rawSql = statementContext.getRawSql();

            if (rawSql == null || rawSql.length() == 0) {
                return EMPTY_SQL;
            }
            return null;
        }
    }

    static final class CheckRawStrategy implements StatementNameStrategy
    {
        private CheckRawStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
            final String rawSql = statementContext.getRawSql();

            if (ClasspathStatementLocator.looksLikeSql(rawSql)) {
                return forRawSql(rawSql);
            }
            return null;
        }
    }

    static final class NaiveNameStrategy implements StatementNameStrategy
    {
        private NaiveNameStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
            final String rawSql = statementContext.getRawSql();

            // Is it using the template loader?
            int colon = rawSql.indexOf(':');

            if (colon == -1) {
                // No package? Just return the name, JDBI figured out somehow on how to find the raw sql for this statement.
                return forRawSql(rawSql);
            }

            final String group = rawSql.substring(0, colon);
            final String name = rawSql.substring(colon + 1);
            return StatementName.getJmxSafeName(group, name, "");
        }
    }

    static final class SqlObjectStrategy implements StatementNameStrategy
    {
        private SqlObjectStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
            final Class<?> clazz = statementContext.getSqlObjectType();
            final Method method = statementContext.getSqlObjectMethod();
            if (clazz != null) {
                final String rawSql = statementContext.getRawSql();

                final String group = clazz.getPackage().getName();
                final String name = clazz.getSimpleName();
                final String type = method == null ? rawSql : method.getName();
                return StatementName.getJmxSafeName(group, name, type);
            }
            return null;
        }
    }

    static final class ContextClassStrategy implements StatementNameStrategy
    {
        private ContextClassStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
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

            return StatementName.getJmxSafeName(className.substring(0, dotPos), className.substring(dotPos+1), statementName);
        }
    }

    static final class ContextNameStrategy implements StatementNameStrategy
    {
        /** File pattern to shorten the group name. */
        private static final Pattern SHORT_PATTERN = Pattern.compile("^(.*?)/(.*?)-sql\\.st$");

        private ContextNameStrategy()
        {
        }

        @Override
        public MetricName getStatementName(final StatementContext statementContext)
        {
            final Object groupObj = statementContext.getAttribute(STATEMENT_GROUP);
            final Object nameObj = statementContext.getAttribute(STATEMENT_NAME);

            if (groupObj == null || nameObj == null) {
                return null;
            }

            final String group = (String) groupObj;
            final String statementName = (String) nameObj;

            final Matcher matcher = SHORT_PATTERN.matcher(group);
            if (matcher.matches()) {
                String groupName = matcher.group(1);
                String typeName = matcher.group(2);
                return StatementName.getJmxSafeName(groupName, typeName, statementName);
            }

            return StatementName.getJmxSafeName(group, statementName, "");
        }
    }

    private NameStrategies()
    {
    }
}
