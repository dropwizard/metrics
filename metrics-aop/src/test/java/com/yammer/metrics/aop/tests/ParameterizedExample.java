package com.yammer.metrics.aop.tests;

import com.yammer.metrics.aop.annotation.Metered;

class ParameterizedExample {
    private final Example example;

    public ParameterizedExample(Example example) {
        this.example = example;
    }

    @Metered
    public String doubleMeteredMethod() {
        return example.meteredMethod();
    }
}
