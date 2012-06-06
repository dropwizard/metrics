package com.yammer.metrics.graphite;

import java.net.Socket;

public interface SocketProvider {
    Socket get() throws Exception;
}
