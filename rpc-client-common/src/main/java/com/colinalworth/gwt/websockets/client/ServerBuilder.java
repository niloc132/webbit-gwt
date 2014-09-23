package com.colinalworth.gwt.websockets.client;

import com.colinalworth.gwt.websockets.shared.Server;

public interface ServerBuilder<S extends Server<S, ?>> {
	/**
	 * Sets the full url, including protocol, host, port, and path for the next server connection
	 * to be started. If called with a non-null value, will override the other setters in this
	 * builder.
	 *
	 * @param url
	 * @return
	 */
	ServerBuilder<S> setUrl(String url);

	/**
	 * Sets the path for the next server to be started. Defaults to the RemoteServiceRelativePath
	 * annotation value for the Server interface, if any.
	 *
	 * @param path
	 * @return
	 */
	ServerBuilder<S> setPath(String path);

	/**
	 * Sets the port of the next server instance to be started. Defaults to the port the current
	 * page loaded from.
	 *
	 * @param port
	 * @return
	 */
	ServerBuilder<S> setPort(int port);

	/**
	 * Sets the hostname for the next server to be started. Defaults to the hostname the current
	 * page loaded from.
	 *
	 * @param hostname
	 * @return
	 */
	ServerBuilder<S> setHostname(String hostname);

	/**
	 * Sets the protocol ("ws" or "wss") to connect with. Defaults to wss if the current page
	 * loaded using https, and ws otherwise.
	 *
	 * @param protocol
	 * @return
	 */
	ServerBuilder<S> setProtocol(String protocol);

	/**
	 * Creates a new instance of the specified server type, starts, and returns it. May
	 * be called more than once to create additional connections, such as after the first
	 * is closed.
	 *
	 * @return
	 */
	S start();

	/**
	 * Specifies a handler to receive errors when a problem occurs with the connection.
	 * @param errorHandler the handler to send connection errors to
	 */
	void setConnectionErrorHandler(ConnectionErrorHandler errorHandler);

	public interface ConnectionErrorHandler {
		void onError(Exception ex);
	}
}
