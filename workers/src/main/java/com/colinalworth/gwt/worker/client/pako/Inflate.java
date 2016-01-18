package com.colinalworth.gwt.worker.client.pako;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;

/**
 * TODO jsinterop
 */
public class Inflate extends JavaScriptObject {
	public static native Inflate create() /*-{
        return new $wnd.pako.Inflate();
    }-*/;

	public native boolean push(ArrayBuffer array, boolean last) /*-{
        return this.push(array, last);
    }-*/;
	public native boolean push(String string, boolean last) /*-{
        return this.push(string, last);
    }-*/;

	public native ArrayBufferView getResult() /*-{
		return this.result;
	-*/;
}
