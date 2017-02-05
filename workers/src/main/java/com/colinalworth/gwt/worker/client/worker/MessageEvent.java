package com.colinalworth.gwt.worker.client.worker;

import jsinterop.annotations.JsFunction;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class MessageEvent {

	@JsProperty
	public native <T> T getData();

	@JsProperty
	public native String getOrigin();

	//ports

	//source

	@JsFunction
	public interface MessageHandler {
		void onMessage(MessageEvent event);
	}
}
