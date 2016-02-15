package com.colinalworth.gwt.worker.client.worker;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Created by colin on 2/10/16.
 */
public final class SharedWorker extends JavaScriptObject {
	protected SharedWorker() {
	}

	public static native SharedWorker create(String path) /*-{
		return new $wnd.SharedWorker(path);
	}-*/;

	public native MessagePort getPort() /*-{
		return this.port;
	}-*/;

}
