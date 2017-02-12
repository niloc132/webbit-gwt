package com.colinalworth.gwt.worker.client.worker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;

/**
 * Created by colin on 1/18/16.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class Worker extends MessagePort {

	public Worker(String path) {

	}

	public native void terminate();
}
