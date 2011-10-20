/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webbitserver.gwt.client.impl;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * Simple JSO wrapper the WebSocket object
 *
 */
public class WebSocket extends JavaScriptObject {
	protected WebSocket() {
		// jso protected ctor
	}
	public interface Callback {
		void onMessage(String data);
		void onError(JavaScriptObject error);
	}
	public static WebSocket create(String server, String path) {

		return create(server, path, null);
	}
	public static native WebSocket create(String server, String path, Callback callback) /*-{
		var ws = new $wnd.WebSocket('ws://' + server + path);
		ws.onmessage = function(e) {
			$entry(callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onMessage(Ljava/lang/String;))(e.data);
		}
		ws.onerror = $entry(callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onError(Lcom/google/gwt/core/client/JavaScriptObject;));
		return ws;
	}-*/;

	/**
	 * 
	 * @param message serialized data to send to the server
	 */
	public native void sendMessage(String message) /*-{
		try {
			this.send(message);
		} catch (e) {
			this.onerror(e);
		}
	}-*/;

	public native void close() /*-{
		this.close();
	}-*/;
}
