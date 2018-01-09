package io.dropwizard.metrics5.jdbi3.strategies;

import io.dropwizard.metrics5.MetricName;
import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.annotation.Timed;
import org.jdbi.v3.core.extension.ExtensionMethod;
import org.jdbi.v3.core.statement.StatementContext;

import java.lang.reflect.Method;

/**
 * Takes into account the {@link Timed} annotation on extension methods
 */
public class TimedAnnotationNameStrategy implements StatementNameStrategy {

    @Override
    public MetricName getStatementName(StatementContext statementContext) {
        final ExtensionMethod extensionMethod = statementContext.getExtensionMethod();
        if (extensionMethod == null) {
            return null;
        }

        final Class<?> clazz = extensionMethod.getType();
        final Timed classTimed = clazz.getAnnotation(Timed.class);
        final Method method = extensionMethod.getMethod();
        final Timed methodTimed = method.getAnnotation(Timed.class);

        // If the method is timed, figure out the name
        if (methodTimed != null) {
            String methodName = methodTimed.name().isEmpty() ? method.getName() : methodTimed.name();
            if (methodTimed.absolute()) {
                return MetricName.build(methodName);
            } else {
                // We need to check if the class has a custom timer name
                return classTimed == null || classTimed.name().isEmpty() ?
                        MetricRegistry.name(clazz, methodName) :
                        MetricRegistry.name(classTimed.name(), methodName);
            }
        } else if (classTimed != null) {
            // Maybe the class is timed?
            return classTimed.name().isEmpty() ? MetricRegistry.name(clazz, method.getName()) :
                    MetricRegistry.name(classTimed.name(), method.getName());
        } else {
            // No timers neither on the method or the class
            return null;
        }
    }
}
