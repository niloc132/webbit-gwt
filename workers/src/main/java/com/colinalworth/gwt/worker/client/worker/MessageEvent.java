package com.colinalworth.gwt.worker.client.worker;

import com.google.gwt.core.client.JavaScriptObject;

public final class MessageEvent extends JavaScriptObject {


	protected MessageEvent() {
	}

	public native <T> T getData() /*-{
		return this.data;
	}-*/;

	public native String getOrigin() /*-{
		return this.origin;
	}-*/;

	//ports

	//source

	public interface MessageHandler {
		void onMessage(MessageEvent event);
	}
}
