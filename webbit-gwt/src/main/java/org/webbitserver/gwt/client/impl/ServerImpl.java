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

import org.webbitserver.gwt.shared.Client;
import org.webbitserver.gwt.shared.Server;

import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;

/**
 * Simple impl of what the client thinks of the server as able to do.
 *
 */
public abstract class ServerImpl<S extends Server<S,C>, C extends Client<C,S>> implements Server<S, C> {
	private C client;
	private final WebSocket connection;

	/**
	 * 
	 */
	public ServerImpl(String path) {
		String server = Window.Location.getHost();
		connection = WebSocket.create(server, path, new WebSocket.Callback() {
			@Override
			public void onMessage(String data) {
				ServerImpl.this.__onMessage(data);
			}
			@Override
			public void onError(JavaScriptObject error) {
				ServerImpl.this.__onError(new JavaScriptException(error));
			}
		});
	}

	protected abstract void __onMessage(String message);
	protected abstract void __onError(Exception error);

	//	public final void __setConnection(WebSocket connection) {
	//		this.connection = connection;
	//	}
	public final WebSocket __getConnection() {
		return connection;
	}

	@Override
	public final void setClient(C client) {
		//TODO open or close connection here?
		this.client = client;
	}
	@Override
	public final C getClient() {
		return client;
	}

	public final void onOpen(org.webbitserver.WebSocketConnection connection, C client) throws Exception {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
	public final void onClose(org.webbitserver.WebSocketConnection connection, C client) throws Exception {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
}
