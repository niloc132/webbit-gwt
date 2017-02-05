package com.colinalworth.gwt.worker.client.worker;

import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import jsinterop.annotations.JsOverlay;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Created by colin on 1/18/16.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class MessagePort {

	public final native void postMessage(Object jso, ArrayBuffer[] buffers);

	public native void start();

	public native void close();

	@JsOverlay
	public final void addMessageHandler(MessageHandler handler) {
		addEventListener("message", handler);
	}

	public native void addEventListener(String type, MessageHandler listener);

}
