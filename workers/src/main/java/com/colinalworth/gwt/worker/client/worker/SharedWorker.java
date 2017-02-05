package com.colinalworth.gwt.worker.client.worker;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Created by colin on 2/10/16.
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public final class SharedWorker {

	public SharedWorker(String path) {

	}
	public SharedWorker(String path, String name) {

	}

	@JsProperty
	public native MessagePort getPort();

}
