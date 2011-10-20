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
package org.webbitserver.gwt.shared;

import org.webbitserver.WebSocketConnection;

/**
 * Basic server interface, to be extended to indicate that methods may be invoked by the client.
 * A server implementation must be provided to a GwtWebService, along with the class object for
 * the matching client.
 *
 */
public interface Server<S extends Server<S,C>, C extends Client<C,S>> {
	/**
	 * Called when a client has initiated a connection to the server. Not to be invoked directly 
	 * by the client.
	 * @param connection
	 * @param client
	 * @throws Exception
	 */
	void onOpen(WebSocketConnection connection, C client) throws Exception;

	/**
	 * Called as a client connection is lost. Not to be invoked directly by the client.
	 * 
	 * @param connection
	 * @param client
	 * @throws Exception
	 */
	void onClose(WebSocketConnection connection, C client) throws Exception;

	/**
	 * Gets the client for the currently running request, if any. Should return the value last
	 * passed to {@link #setClient(Client)} (if on the server, within the current thread).
	 * @return
	 */
	C getClient();

	/**
	 * Sets the current client to call back to. Called on the server to indicate that the given
	 * client instance is about to make a call. Called by the client to specify which client
	 * instance should have messages forwarded to it.
	 * 
	 * @param client
	 */
	void setClient(C client);
}
