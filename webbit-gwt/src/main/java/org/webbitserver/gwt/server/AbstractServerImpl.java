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
 * Simple starting point for implementing a set of calls reachable from a webbit-gwt client
 *
 */
public abstract class AbstractServerImpl<S extends Server<S,C>, C extends Client<C,S>> implements Server<S, C> {
	private final ThreadLocal<C> currentClient = new ThreadLocal<C>();

	@Override
	public void onOpen(WebSocketConnection connection, C client) throws Exception {
	}

	@Override
	public void onClose(WebSocketConnection connection, C client) throws Exception {
	}

	@Override
	public C getClient() {
		return currentClient.get();
	}

	@Override
	public void setClient(C client) {
		currentClient.set(client);
	}

}
