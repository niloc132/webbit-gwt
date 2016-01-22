package com.colinalworth.gwt.worker.client.worker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Created by colin on 1/18/16.
 */
public final class Worker extends MessagePort {
	protected Worker() {
	}

	public static native Worker child(String path) /*-{
		return new $wnd.Worker(path);
	}-*/;



	public native void terminate() /*-{
		this.terminate();
	}-*/;
}
