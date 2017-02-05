package com.colinalworth.gwt.worker.client.pako;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "pako")
public class Deflate {
	public native boolean push(ArrayBuffer array, boolean last);
	public native boolean push(ArrayBufferView array, boolean last);
	public native boolean push(String string, boolean last);

	@JsProperty
	public native ArrayBufferView getResult();

}
