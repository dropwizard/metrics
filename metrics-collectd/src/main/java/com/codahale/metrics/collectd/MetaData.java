package com.codahale.metrics.collectd;

final class MetaData {

    private String host;
    private String plugin;
    private String pluginInstance;
    private String type;
    private String typeInstance;
    private long timestamp;
    private long period;

    private MetaData() {}

    private MetaData(MetaData other) {
        this.host = other.host;
        this.plugin = other.plugin;
        this.pluginInstance = other.pluginInstance;
        this.type = other.type;
        this.typeInstance = other.typeInstance;
        this.timestamp = other.timestamp;
        this.period = other.period;
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

        private MetaData metaData = new MetaData();

        Builder(String host, long timestamp, long duration) {
            metaData.host = Sanitize.instanceName(host);
            metaData.timestamp = timestamp;
            metaData.period = duration;
        }

        Builder plugin(String name) {
            metaData.plugin = Sanitize.name(name);
            return this;
        }

        Builder pluginInstance(String name) {
            metaData.pluginInstance = Sanitize.instanceName(name);
            return this;
        }

        Builder type(String name) {
            metaData.type = Sanitize.name(name);
            return this;
        }

        Builder typeInstance(String name) {
            metaData.typeInstance = Sanitize.instanceName(name);
            return this;
        }

        MetaData get() {
            return new MetaData(metaData);
        }
    }
}
