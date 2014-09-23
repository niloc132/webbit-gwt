package com.colinalworth.gwt.websockets.shared;

/**
 * Starting interface for building the methods the server may call on the client. User code may
 * provide an instance of this type to a Server instance created using GWT.create, and Client
 * instances will be created automatically on the server as connections are created.
 *
 * @see Server
 *
 */

public interface Client<C extends Client<C,S>, S extends Server<S,C>> {

	/**
	 * Callback called when the connection to the server has been established. Should not be called
	 * directly from the server.
	 */
	void onOpen();

	/**
	 * Callback called when the connection to the server has been closed. Should not be called
	 * directly from the server.
	 */
	void onClose();

	/**
	 * Called when an error occurs while trying to send a message to the server.
	 * @param error the error that occurred.
	 */
	void onError(Throwable error);
}
