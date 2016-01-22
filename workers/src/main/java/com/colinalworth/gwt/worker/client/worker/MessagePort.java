package com.colinalworth.gwt.worker.client.worker;

import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.typedarrays.shared.ArrayBuffer;

/**
 * Created by colin on 1/18/16.
 */
public class MessagePort extends JavaScriptObject {

	protected MessagePort() {
	}

	public final native void postMessage(String str) /*-{
		this.postMessage(str);
	}-*/;

	public final native void postMessage(JavaScriptObject jso) /*-{
		this.postMessage(jso);
	}-*/;

	public final native void postMessage(JavaScriptObject jso, ArrayBuffer buffer) /*-{
		this.postMessage(jso, [buffer]);
	}-*/;

	public final native void postMessage(JavaScriptObject jso, ArrayBuffer... buffers) /*-{
		this.postMessage(jso, buffers);
	}-*/;


	public final native void start() /*-{
		this.start();
	}-*/;

	public final native void close() /*-{
		this.close();
	}-*/;

	public final native void addMessageHandler(MessageHandler handler) /*-{
		this.addEventListener("message", $entry(function(event) {
			handler.@com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler::onMessage(*)(event);
		}));
	}-*/;


}
