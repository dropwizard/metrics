package com.codahale.metrics.annotation;

/**
 * ResponseMeteredLevel is a parameter for the ResponseMetered annotation. The constants of this enumerated type
 * decide what meters are included when a class or method is annotated with the ResponseMetered annotation.
 *
 */
public enum ResponseMeteredLevel {
    /**
     * Will include meters for 1xx/2xx/3xx/4xx/5xx responses
     */
    COARSE,

    /**
     * Will include meters for every response code
     */
    DETAILED,

    /**
     * Will include meters for every response code in addition to top level 1xx/2xx/3xx/4xx/5xx responses
     */
    ALL;
}
