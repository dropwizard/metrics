package com.codahale.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class that catches uncaught {@link java.lang.Throwable} and log it
 * @author Enno Shioji (eshioji@gmail.com)
 */
public abstract class VerboseRunnable implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(VerboseRunnable.class);


    @Override
    public final void run() {
        try{
            doRun();
        }catch (Throwable e) {
            log.error("Uncaught throwable", e);
            throw new RuntimeException(e);
        }
    }

    protected abstract void doRun();
}
