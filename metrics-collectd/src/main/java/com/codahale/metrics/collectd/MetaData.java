package com.codahale.metrics.collectd;

class MetaData {

    private final String host;
    private final String plugin;
    private final String pluginInstance;
    private final String type;
    private final String typeInstance;
    private final long timestamp;
    private final long period;

    MetaData(String host, String plugin, String pluginInstance, String type, String typeInstance,
             long timestamp, long period) {
        this.host = host;
        this.plugin = plugin;
        this.pluginInstance = pluginInstance;
        this.type = type;
        this.typeInstance = typeInstance;
        this.timestamp = timestamp;
        this.period = period;
    }

    String getHost() {
        return host;
    }

    String getPlugin() {
        return plugin;
    }

    String getPluginInstance() {
        return pluginInstance;
    }

    String getType() {
        return type;
    }

    String getTypeInstance() {
        return typeInstance;
    }

    long getTimestamp() {
        return timestamp;
    }

    long getPeriod() {
        return period;
    }

    static class Builder {

        private String host;
        private String plugin;
        private String pluginInstance;
        private String type;
        private String typeInstance;
        private long timestamp;
        private long period;

        Builder(String host, long timestamp, long duration) {
            this.host = Sanitize.instanceName(host);
            this.timestamp = timestamp;
            period = duration;
        }

        Builder plugin(String name) {
            plugin = Sanitize.name(name);
            return this;
        }

        Builder pluginInstance(String name) {
            pluginInstance = Sanitize.instanceName(name);
            return this;
        }

        Builder type(String name) {
            type = Sanitize.name(name);
            return this;
        }

        Builder typeInstance(String name) {
            typeInstance = Sanitize.instanceName(name);
            return this;
        }

        MetaData get() {
            return new MetaData(host, plugin, pluginInstance, type, typeInstance, timestamp, period);
        }
    }
}
