package com.yammer.metrics.aop;

import java.lang.reflect.Field;

class FieldCopier {
    public <T> void copyValue(T from, T to, Field field) throws IllegalAccessException {
        field.set(to, field.get(from));
    }
}
