package com.codahale.metrics;



public class UserTimeClockHolder {
    public static final Clock DEFAULT = new UserTimeClock();
}
