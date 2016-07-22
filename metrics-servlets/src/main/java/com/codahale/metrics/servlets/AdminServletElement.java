package com.codahale.metrics.servlets;

import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import static java.util.Collections.unmodifiableList;

public class AdminServletElement {

    public static AdminServletElementBuilder onPath(String uri) {
        return new AdminServletElementBuilder(uri);
    }
    private final String uri;
    private final Servlet servlet;
    private final List<Link> links;

    private AdminServletElement(String uri, Servlet servlet, List<Link> links) {
        this.uri = uri;
        this.servlet = servlet;
        this.links = unmodifiableList(links);
    }

    public Servlet getServlet() {
        return servlet;
    }

    public String getUri() {
        return uri;
    }

    public List<Link> getLinks() {
        return links;
    }

    public static class AdminServletElementBuilder {

        private final String uri;
        private Servlet servlet;
        private final List<Link> links = new ArrayList<Link>();

        public AdminServletElementBuilder(String uri) {
            this.uri = uri;
        }

        public AdminServletElementBuilder forServlet(Servlet servlet) {
            this.servlet = servlet;
            return this;
        }

        public AdminServletElementBuilder addLink(Link link) {
            this.links.add(link);
            return this;
        }

        public AdminServletElement build() {
            return new AdminServletElement(uri, servlet, links);
        }
    }
}
