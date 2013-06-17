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
package org.webbitserver.gwt.server;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.gwt.shared.Client;
import org.webbitserver.gwt.shared.Server;

/**
 * Simple starting point for implementing a set of calls reachable from a webbit-gwt client.
 * Provides thread local access to the current Client impl for any given method invocation
 * through setClient and getClient.
 * 
 * The methods onOpen and onClose have default implementations that do nothing.
 *
 */
public abstract class AbstractServerImpl<S extends Server<S,C>, C extends Client<C,S>> implements Server<S, C> {
	private final ThreadLocal<C> currentClient = new ThreadLocal<C>();

	@Override
	public void onOpen(WebSocketConnection connection, C client) {
		// default empty implementation to allow clients to define this only if desired
	}

	@Override
	public void onClose(WebSocketConnection connection, C client) {
		// default empty implementation to allow clients to define this only if desired
	}

	@Override
	public void onError(Throwable error) {
		System.err.println("The following error occurred while executing a server method - override onError to prevent/replace this message");
		error.printStackTrace();
	}

	@Override
	public C getClient() {
		return currentClient.get();
	}

	@Override
	public void setClient(C client) {
		currentClient.set(client);
	}

	@Override
	public final void close() {
		throw new IllegalStateException("This method may not be called on the server, only on the client. To close the connection, invoke WebSocketConnection.close() on the connection you want to stop.");
	}
}
