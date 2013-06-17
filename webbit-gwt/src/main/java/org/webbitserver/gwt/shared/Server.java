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
 * On the client, a Server implementation can be obtained using GWT.create, and a matching Client
 * implementation must be provided or messages sent from the server will be ignored.
 * 
 * Must be annotated with {@link com.google.gwt.user.client.rpc.RemoteServiceRelativePath} to
 * indicate where this resource can be found on the server.
 * 
 * @see Client
 * 
 */
public interface Server<S extends Server<S,C>, C extends Client<C,S>> {
	/**
	 * Called when a client has initiated a connection to the server. Not to be invoked directly 
	 * by the client.
	 * @param connection
	 * @param client
	 */
	void onOpen(WebSocketConnection connection, C client);

	/**
	 * Called as a client connection is lost. Not to be invoked directly by the client.
	 * 
	 * @param connection
	 * @param client
	 */
	void onClose(WebSocketConnection connection, C client);

	void onError(Throwable error);

	/**
	 * Gets the client for the currently running request, if any. Should return the value last
	 * passed to {@link #setClient(Client)} (if on the server, within the current thread).
	 * @return the current client object in use
	 */
	C getClient();

	/**
	 * Sets the current client to call back to. Called on the server to indicate that the given
	 * client instance is about to make a call. Called by the client to specify which client
	 * instance should have messages forwarded to it.
	 * 
	 * @param client the client object to use on this server
	 */
	void setClient(C client);

	/**
	 * Closes the connection on the client. Should not be called in server code.
	 */
	void close();
}
