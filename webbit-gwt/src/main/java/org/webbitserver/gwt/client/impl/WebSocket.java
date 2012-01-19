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
 * Simple JSO wrapper the WebSocket object.
 *
 */
public class WebSocket extends JavaScriptObject {
	protected WebSocket() {
		// jso protected ctor
	}
	public interface Callback {
		void onOpen();
		void onClose();
		void onMessage(String data);
		void onError(JavaScriptObject error);
	}

	public static native WebSocket create(String url, Callback callback) /*-{
		var ws = (!!$wnd.WebSocket) ? new $wnd.WebSocket(url) : $wnd.MozWebSocket(url);
		ws.onopen = $entry(function(){callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onOpen()()});
		ws.onclose = $entry(function(){callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onClose()()});
		ws.onmessage = $entry(function(e) {
			callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onMessage(Ljava/lang/String;)(e.data);
		});
		ws.onerror = $entry(function(e){callback.@org.webbitserver.gwt.client.impl.WebSocket.Callback::onError(Lcom/google/gwt/core/client/JavaScriptObject;)(e)});
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
