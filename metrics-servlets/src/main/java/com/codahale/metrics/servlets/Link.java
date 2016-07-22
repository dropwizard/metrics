package com.codahale.metrics.servlets;

public class Link {

    public static LinkBuilder to(String path) {
        return new LinkBuilder(path);
    }
    private final String label;
    private final String path;

    private Link(String label, String path) {
        this.label = label;
        this.path = path;
    }

    public String getLabel() {
        return label;
    }

    public String getPath() {
        return path;
    }

    public static class LinkBuilder {

        private final String path;
        private String label;

        public LinkBuilder(String path) {
            this.path = path;
        }

        public LinkBuilder withLabel(String label) {
            this.label = label;
            return this;
        }
        
        public Link build() {
            return new Link(label, path);
        }
    }
    
}
