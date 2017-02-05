package com.colinalworth.gwt.websockets.client.impl;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.user.client.Event;
import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Simple JSO wrapper the WebSocket object.
 *
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class WebSocket {

    public static int CLOSED;
    public static int CLOSING;
    public static int CONNECTING;
    public static int OPEN;

    public String binaryType;
    public int bufferedAmount;
    public OnCloseCallback onclose;
    public OnMessageCallback onmessage;
    public OnOpenCallback onopen;
    public OnErrorCallback onerror;
    public int readyState;
    public String url;

    public WebSocket(String url) {
    }

    public native boolean send(ArrayBufferView data);

    public native boolean send(String data);

    public native boolean send(ArrayBuffer data);

	public native void close();

    @JsFunction
    public interface OnCloseCallback {
        void onClose(Event a);

    }
    @JsFunction
    public interface OnMessageCallback<T> {
        void onMessage(MessageEvent<T> a);

    }
    @JsFunction
    public interface OnOpenCallback {
        void onOpen(Event a);
    }

    @JsFunction
    public interface OnErrorCallback {
        void onError(JavaScriptObject error);
    }

    @JsType(isNative = true, namespace = JsPackage.GLOBAL)
    public static class MessageEvent<T> {
        public T data;
    }
}
