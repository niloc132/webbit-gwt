package com.colinalworth.gwt.websockets.client.impl;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;
import com.colinalworth.gwt.websockets.shared.impl.ClientInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerInvocation;
import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;

/**
 * Simple impl of what the client thinks of the server as able to do. Used as a base class for
 * generated server impls, should not be directly referenced in client code.
 *
 * Some method names in this class are prefixed with "__" to ensure that they cannot collide
 * with actual methods in the interface being implemented.
 *
 */
public abstract class ServerImpl<S extends Server<S,C>, C extends Client<C,S>> implements Server<S, C> {
	private C client;
	private final WebSocket connection;
	private final ServerBuilder.ConnectionErrorHandler errorHandler;

	/**
	 * annoying useless constructor that doesn't do anything meaningful to let subclass throw an exception
	 */
	public ServerImpl(Void nothing) {
		connection = null;
		errorHandler = null;
	}

	/**
	 * Creates a server impl, communicating with the host page's server, on the given path.
	 *
	 * @param errorHandler handler to notify in case of connection error
	 * @param url absolute path to use to communicate with the server
	 */
	public ServerImpl(final ServerBuilder.ConnectionErrorHandler errorHandler, String url) {
		this.errorHandler = errorHandler;
		connection = WebSocket.create(url, new WebSocket.Callback() {
			@Override
			public void onOpen(JavaScriptObject event) {
				C client = __checkClient();
				if (client != null) {
					client.onOpen();
				}
			}
			@Override
			public void onClose(JavaScriptObject event) {
				C client = __checkClient();
				if (client != null) {
					client.onClose();
				}
			}
			@Override
			public void onMessage(String data) {
				try {
					ServerImpl.this.__onMessage(data);
				} catch (Exception e) {
					onError(e);
				}
			}
			private void onError(Exception e) {
				if (errorHandler != null) {
					errorHandler.onError(e);
				} else {
					GWT.log("A transport error occurred - pass a error handler to your server builder to handle this yourself", e);
				}
			}
			@Override
			public void onError(JavaScriptObject error) {
				onError(new JavaScriptException(error));
			}
		});
	}

	public ServerImpl(ServerBuilder.ConnectionErrorHandler errorHandler, String protocol, String server, String path) {
		this(errorHandler, protocol + server + path);
	}

	/**
	 * Provides an instance of a Serializer that can be used to send and receive messages.
	 *
	 * @return a custom serializer for the current Server/Client pair.
	 */
	protected abstract Serializer __getSerializer();

	/**
	 * Internal method to handle incoming messages and to push them through to the current
	 * Client instance after deserialization.
	 *
	 * @param message incoming message
	 * @throws com.google.gwt.user.client.rpc.SerializationException thrown if the client is unable to handle the message sent
	 * by the server
	 */
	private void __onMessage(String message) throws SerializationException {
		__checkClient();
		assert message.startsWith("//OK");//consider axing this, and the substring below
		ClientSerializationStreamReader clientSerializationStreamReader = new ClientSerializationStreamReader(__getSerializer());
		clientSerializationStreamReader.prepareToRead(message.substring(4));

		ClientInvocation decodedMessage = (ClientInvocation) clientSerializationStreamReader.readObject();
		try {
			__invoke(decodedMessage.getMethod(), decodedMessage.getParameters());
		} catch (Exception ex) {
			//pass any error when invoking a client method back to the error handler
			getClient().onError(ex);
		}
	}

	/**
	 * Serializes and send a message to the server based on a method invocation made.
	 *
	 * @param methodName
	 * @param params
	 */
	protected void __sendMessage(String methodName, Object... params) {
		ServerInvocation invoke = new ServerInvocation(methodName, params);

		AbstractSerializationStreamWriter writer = new ClientSerializationStreamWriter(__getSerializer(), "", "101010");
		// manually prepare to serialize a method call to keep the server side simpler
		writer.prepareToWrite();
		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.WebbitService");//interface
		writer.writeString("dummy");//method name
		writer.writeInt(1);//param count

		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.ServerInvocation");//param type

		try {
			// actually encode the object to send
			writer.writeObject(invoke);

			// send the message over the wire
			__getConnection().sendMessage(writer.toString());
		} catch (SerializationException e) {
			// report the error, then throw an exception. This is too important of an error to
			// let it be ignored
			if (errorHandler != null) {
				errorHandler.onError(e);
			} else {
				GWT.log("A serialization error occurred - pass a error handler to your server builder to handle this yourself", e);
			}

			//throw the exception so that any calling code can handle it
			throw new RuntimeException(e);
		}
	}

	/**
	 * Method to be replaced in actual implementation by the equivalent of
	 * method.invoke(this, params), based on the known possible methods that can be run.
	 * @param method
	 * @param params
	 */
	protected abstract void __invoke(String method, Object[] params);

	public final WebSocket __getConnection() {
		return connection;
	}

	@Override
	public final void setClient(C client) {
		this.client = client;
	}
	@Override
	public final C getClient() {
		return client;
	}

	private final C __checkClient() {
		if (!GWT.isProdMode() && getClient() == null) {
			GWT.log("Client has not been assigned for " + getClass() + ": make sure to call setClient to receive server messages.");
		}
		return getClient();
	}

	@Override
	public final void onOpen(Connection connection, C client) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
	@Override
	public final void onClose(Connection connection, C client) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
	@Override
	public final void onError(Throwable error) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}

	@Override
	public final void close() {
		__getConnection().close();
	}
}
