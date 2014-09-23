package com.colinalworth.gwt.websockets.shared;

public interface Server<S extends Server<S,C>, C extends Client<C,S>> {

	/**
	 * Called when a client has initiated a connection to the server. Not to be invoked directly
	 * by the client.
	 * @param connection
	 * @param client
	 */
	void onOpen(Connection connection, C client);

	/**
	 * Called as a client connection is lost. Not to be invoked directly by the client.
	 *
	 * @param connection
	 * @param client
	 */
	void onClose(Connection connection, C client);

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


	public static interface Connection {
		void data(String key, Object value);
		Object data(String key);

		void close();
	}
}
