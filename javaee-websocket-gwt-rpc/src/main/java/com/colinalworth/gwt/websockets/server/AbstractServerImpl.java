package com.colinalworth.gwt.websockets.server;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;

public abstract class AbstractServerImpl<S extends Server<S,C>, C extends Client<C,S>> extends RpcEndpoint<S, C> implements Server<S, C> {
	/** In JSR-356, each server socket instance has exactly one client */
	private C client;

	protected AbstractServerImpl(Class<C> client) {
		super(client);
	}

	@Override
	public void onOpen(Connection connection, C client) {
		// default empty implementation to allow clients to define this only if desired
	}

	@Override
	public void onClose(Connection connection, C client) {
		// default empty implementation to allow clients to define this only if desired
	}

	@Override
	public C getClient() {
		return client;
	}

	@Override
	public void setClient(C client) {
		this.client = client;
	}

	@Override
	public final void close() {
		throw new IllegalStateException("This method may not be called on the server, only on the client. To close the connection, invoke WebSocketConnection.close() on the connection you want to stop.");
	}
}
