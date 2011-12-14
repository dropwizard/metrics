package com.yammer.metrics.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

class LenientCopyTool {
    FieldCopier fieldCopier = new FieldCopier();

    public <T> void copyToMock(T from, T mock) {
        copy(from, mock, from.getClass(), mock.getClass().getSuperclass());
    }

    public <T> void copyToRealObject(T from, T to) {
        copy(from, to, from.getClass(), to.getClass());
    }

    private <T> void copy(T from, T to, Class fromClazz, Class toClass) {
        while (fromClazz != Object.class) {
            copyValues(from, to, fromClazz);
            fromClazz = fromClazz.getSuperclass();
        }
    }

    private <T> void copyValues(T from, T mock, Class classFrom) {
        for (Field field : classFrom.getDeclaredFields()) {
            // ignore static fields
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            final AccessibilityChanger accessibilityChanger = new AccessibilityChanger();
            try {
                accessibilityChanger.enableAccess(field);
                fieldCopier.copyValue(from, mock, field);
            } catch (Throwable t) {
                //Ignore - be lenient - if some field cannot be copied then let's be it
            } finally {
                accessibilityChanger.safelyDisableAccess(field);
            }
        }
    }
}
