package com.codahale.metrics.collectd;

/**
 * Constants from collectd/src/network.h and jcd.* property configuration.
 */
class Network {

    public static final int TYPE_HOST            = 0x0000;
    public static final int TYPE_TIME            = 0x0001;
    public static final int TYPE_PLUGIN          = 0x0002;
    public static final int TYPE_PLUGIN_INSTANCE = 0x0003;
    public static final int TYPE_TYPE            = 0x0004;
    public static final int TYPE_TYPE_INSTANCE   = 0x0005;
    public static final int TYPE_VALUES          = 0x0006;
    public static final int TYPE_INTERVAL        = 0x0007;

    static final int UINT8_LEN  = 1;
    static final int UINT16_LEN = UINT8_LEN * 2;
    static final int UINT32_LEN = UINT16_LEN * 2;
    static final int UINT64_LEN = UINT32_LEN * 2;
    static final int HEADER_LEN = UINT16_LEN * 2;
    static final int BUFFER_SIZE = 1024; // as per collectd/src/network.c

}
