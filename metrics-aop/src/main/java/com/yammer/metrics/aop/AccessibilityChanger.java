package com.yammer.metrics.aop;

import java.lang.reflect.Field;

class AccessibilityChanger {
    private Boolean wasAccessible = null;

    public void safelyDisableAccess(Field field) {
        assert wasAccessible != null;
        try {
            field.setAccessible(wasAccessible);
        } catch (Throwable t) {
            //ignore
        }
    }

    public void enableAccess(Field field) {
        wasAccessible = field.isAccessible();
        field.setAccessible(true);
    }
}
