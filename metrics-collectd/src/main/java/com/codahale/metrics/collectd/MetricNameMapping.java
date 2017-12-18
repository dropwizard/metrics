package com.codahale.metrics.collectd;

import java.util.StringTokenizer;

final class MetricNameMapping {

    private static final String SEPARATOR = ".";

    static Identifier.Builder createIdentifier(String host, String metricName) {
        Identifier.Builder builder = new Identifier.Builder(host);
        StringTokenizer tokenizer = new StringTokenizer(metricName, SEPARATOR);
        switch (tokenizer.countTokens()) {
            case 1:  return a(builder, tokenizer);
            case 2:  return b(builder, tokenizer);
            default: return c(builder, tokenizer);
        }
    }

    private static Identifier.Builder a(Identifier.Builder builder, StringTokenizer tokenizer) {
        String name = tokenizer.nextToken();
        return builder.plugin(name).type(name);
    }

    private static Identifier.Builder b(Identifier.Builder builder, StringTokenizer tokenizer) {
        return builder.plugin(tokenizer.nextToken()).type(tokenizer.nextToken());
    }

    private static Identifier.Builder c(Identifier.Builder builder, StringTokenizer tokenizer) {
        return builder
            .plugin(tokenizer.nextToken())
            .pluginInstance(tokenizer.nextToken())
            .type(joinTokens(tokenizer));
    }

    private static String joinTokens(StringTokenizer tokenizer) {
        StringBuilder buffer = new StringBuilder(tokenizer.nextToken());
        while (tokenizer.hasMoreTokens()) {
            buffer.append(SEPARATOR).append(tokenizer.nextToken());
        }
        return buffer.toString();
    }
}
