package com.codahale.metrics.annotation;

/**
 * {@link ResponseMeteredLevel} is a parameter for the {@link ResponseMetered} annotation.
 * The constants of this enumerated type decide what meters are included when a class
 * or method is annotated with the {@link ResponseMetered} annotation.
 */
public enum ResponseMeteredLevel {
    /**
     * Include meters for 1xx/2xx/3xx/4xx/5xx responses
     */
    COARSE,

    /**
     * Include meters for every response code (200, 201, 303, 304, 401, 404, 501, etc.)
     */
    DETAILED,

    /**
     * Include meters for every response code in addition to top level 1xx/2xx/3xx/4xx/5xx responses
     */
    ALL;
}
