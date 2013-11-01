package com.codahale.metrics.servlets;

import java.util.ArrayList;
import java.util.List;

import static java.text.MessageFormat.format;


public final class AdminPage {

    private final String pageTemplate;

    public AdminPage(String pageTemplate) {
        this.pageTemplate = pageTemplate;
    }

    public final String menu(Object path) {
        return format(pageTemplate, path);
    }

    public static class Builder {

        private static final String prefix = String.format(
                "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\"'%n'" +
                "        \"http://www.w3.org/TR/html4/loose.dtd\">%n" +
                "<html>%n" +
                "<head>%n" +
                "  <title>Metrics</title>%n" +
                "</head>%n" +
                "<body>%n" +
                "  <h1>Operational Menu</h1>%n");
        private static final String suffix = String.format("</body>%n</html>");
        private final List<String> links = new ArrayList<String>();

        public void addItem(String uri, String displayName, boolean prettyPrintSupported) {
            links.add(link(uri, displayName, prettyPrintSupported));
        }

        public AdminPage build() {
            return new AdminPage(prefix + menuTemplate() + suffix);
        }

        private String menuTemplate() {
            StringBuilder menuItems = new StringBuilder();
            menuItems.append("  <ul>%n");
            for (String link : links) {
                menuItems.append(link);
            }
            menuItems.append("  </ul>%n");
            return String.format(menuItems.toString());
        }

        private String link(String uri, String displayName, boolean prettyPrintSupported) {
            if(prettyPrintSupported) {
                return String.format("    <li><a href=\"{0}%s?pretty=true\">%s</a></li>%n", uri, displayName);
            }
            return String.format("    <li><a href=\"{0}%s\">%s</a></li>%n", uri, displayName);
        }

    }
}
