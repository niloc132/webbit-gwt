package com.colinalworth.gwt.websockets.client.impl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Simple JSO wrapper the WebSocket object.
 *
 */
public class WebSocket extends JavaScriptObject {
	protected WebSocket() {
		// jso protected ctor
	}
	public interface Callback {
		void onOpen(JavaScriptObject event);
		void onClose(JavaScriptObject event);
		void onMessage(String data);
		void onError(JavaScriptObject error);
	}

	public static native WebSocket create(String url, Callback callback) /*-{
      var ws = (!!$wnd.WebSocket) ? new $wnd.WebSocket(url) : $wnd.MozWebSocket(url);
      ws.onopen = $entry(function(e){callback.@com.colinalworth.gwt.websockets.client.impl.WebSocket.Callback::onOpen(*)(e)});
      ws.onclose = $entry(function(e){callback.@com.colinalworth.gwt.websockets.client.impl.WebSocket.Callback::onClose(*)(e)});
      ws.onmessage = $entry(function(e) {
          callback.@com.colinalworth.gwt.websockets.client.impl.WebSocket.Callback::onMessage(Ljava/lang/String;)(e.data);
      });
      ws.onerror = $entry(function(e){callback.@com.colinalworth.gwt.websockets.client.impl.WebSocket.Callback::onError(*)(e)});
      return ws;
  }-*/;

	/**
	 *
	 * @param message serialized data to send to the server
	 */
	public native final void sendMessage(String message) /*-{
      try {
          this.send(message);
      } catch (e) {
          this.onerror(e);
      }
  }-*/;

	public native final void close() /*-{
      this.close();
  }-*/;
}
