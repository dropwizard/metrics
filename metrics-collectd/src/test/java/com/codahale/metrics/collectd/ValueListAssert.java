package com.codahale.metrics.collectd;

import org.assertj.core.api.AbstractAssert;
import org.collectd.api.ValueList;

import java.util.Objects;

public final class ValueListAssert extends AbstractAssert<ValueListAssert, ValueList> {

    public ValueListAssert(ValueList actual) {
        super(actual, ValueListAssert.class);
    }

    public static ValueListAssert assertThat(ValueList actual) {
        return new ValueListAssert(actual);
    }

    public ValueListAssert hasHost(String host) {
        isNotNull();
        return equals("host", actual.getHost(), host);
    }

    public ValueListAssert fromPlugin(String plugin, String pluginInstance) {
        isNotNull();
        equals("plugin name", actual.getPlugin(), plugin);
        return equals("plugin instance", actual.getPluginInstance(), pluginInstance);
    }

    public ValueListAssert fromPlugin(String plugin) {
        return fromPlugin(plugin, "");
    }

    public ValueListAssert hasType(String type, String typeInstance) {
        isNotNull();
        equals("type", actual.getType(), type);
        return equals("type instance", actual.getTypeInstance(), typeInstance);
    }

    public ValueListAssert hasType(String type) {
        return hasType(type, "");
    }

    private ValueListAssert equals(String name, Object actual, Object expected) {
        if (!Objects.equals(actual, expected)) {
            failWithMessage("Expected %s <%s> but was <%s>", name, expected, actual);
        }
        return this;
    }

}
