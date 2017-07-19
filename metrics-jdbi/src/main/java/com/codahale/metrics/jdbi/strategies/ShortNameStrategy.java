package com.codahale.metrics.jdbi.strategies;

import org.skife.jdbi.v2.StatementContext;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Assembles all JDBI stats under a common prefix (passed in at constructor time). Stats are grouped
 * by class name and method; a shortening strategy is applied to make the JMX output nicer.
 */
public final class ShortNameStrategy extends DelegatingStatementNameStrategy {
    private final ConcurrentMap<String, String> shortClassNames = new ConcurrentHashMap<>();

    private final String baseJmxName;

    public ShortNameStrategy(String baseJmxName) {
        this.baseJmxName = baseJmxName;

        // Java does not allow super (..., new ShortContextClassStrategy(), new ShortSqlObjectStrategy(), ...);
        // ==> No enclosing instance of type <xxx> is available due to some intermediate constructor invocation. Lame.
        registerStrategies(NameStrategies.CHECK_EMPTY,
                           new ShortContextClassStrategy(),
                           new ShortSqlObjectStrategy(),
                           NameStrategies.CHECK_RAW,
                           NameStrategies.NAIVE_NAME);
    }

    private final class ShortContextClassStrategy implements StatementNameStrategy {
        @Override
        public String getStatementName(StatementContext statementContext) {
            final Object classObj = statementContext.getAttribute(NameStrategies.STATEMENT_CLASS);
            final Object nameObj = statementContext.getAttribute(NameStrategies.STATEMENT_NAME);

            if (classObj == null || nameObj == null) {
                return null;
            }

            final String className = (String) classObj;
            final String statementName = (String) nameObj;

            final int dotPos = className.lastIndexOf('.');
            if (dotPos == -1) {
                return null;
            }

            final String shortName = className.substring(dotPos + 1);

            final String oldClassName = shortClassNames.putIfAbsent(shortName, className);
            if (oldClassName == null || oldClassName.equals(className)) {
                return name(baseJmxName, shortName, statementName);
            } else {
                return name(baseJmxName, className, statementName);
            }
        }
    }

    private final class ShortSqlObjectStrategy implements StatementNameStrategy {
        @Override
        public String getStatementName(StatementContext statementContext) {
            final Class<?> clazz = statementContext.getSqlObjectType();
            final Method method = statementContext.getSqlObjectMethod();
            if (clazz != null && method != null) {
                final String className = clazz.getName();
                final String statementName = method.getName();

                final int dotPos = className.lastIndexOf('.');
                if (dotPos == -1) {
                    return null;
                }

                final String shortName = className.substring(dotPos + 1);

                final String oldClassName = shortClassNames.putIfAbsent(shortName, className);
                if (oldClassName == null || oldClassName.equals(className)) {
                    return name(baseJmxName, shortName, statementName);
                } else {
                    return name(baseJmxName, className, statementName);
                }
            }
            return null;
        }
    }
}
