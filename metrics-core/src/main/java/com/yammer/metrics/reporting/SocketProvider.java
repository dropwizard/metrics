package com.yammer.metrics.reporting;

import java.net.Socket;

public interface SocketProvider {
    Socket get() throws Exception;
}
