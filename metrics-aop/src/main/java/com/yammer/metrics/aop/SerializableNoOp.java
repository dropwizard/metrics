package com.yammer.metrics.aop;

import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.NoOp;

import java.io.Serializable;

class SerializableNoOp implements NoOp, Serializable {
    private static final long serialVersionUID = 7434976328690189159L;
    public static final Callback SERIALIZABLE_INSTANCE = new SerializableNoOp();
}
