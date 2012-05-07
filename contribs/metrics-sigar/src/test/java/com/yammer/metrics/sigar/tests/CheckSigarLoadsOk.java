package com.yammer.metrics.sigar.tests;

import org.hyperic.sigar.Sigar;
import org.junit.BeforeClass;

import static org.junit.Assume.assumeNoException;

/**
 * Created: 12/05/07 17:28
 *
 * @author chris
 */
public abstract class CheckSigarLoadsOk {

    @BeforeClass
    public static final void canLoadSigarCheck() {
        try {
            Sigar.load();
        } catch (Throwable e) {
            assumeNoException(e);
        }
    }

}
