package org.webbitserver;

import java.util.Set;
import java.util.Map;

public interface WebSocketConnection {
    Object httpRequest();

    Object send(String message);

    Object close();

    Object data(String key, Object value);

    Object handlerExecutor();

    void execute(java.lang.Runnable arg0);

    Map<String, Object> data();

    Object data(String key);

    Set<String> dataKeys();
}