package com.codahale.metrics.collectd;

final class Identifier {

    public final String host;
    public final String plugin;
    public final String pluginInstance;
    public final String type;
    public final String typeInstance;

    private Identifier(String host, String plugin, String pluginInstance, String type, String typeInstance) {
        this.host = host;
        this.plugin = plugin;
        this.pluginInstance = pluginInstance;
        this.type = type;
        this.typeInstance = typeInstance;
    }

    static class Builder {

        private final String host;
        private String plugin;
        private String pluginInstance;
        private String type;
        private String typeInstance;

        Builder(String host) {
            this.host = Sanitize.instanceName(host);
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

        Identifier get() {
            return new Identifier(host, plugin, pluginInstance, type, typeInstance);
        }
    }
}


