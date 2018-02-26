/*
 * #%L
 * rpc-client-common
 * %%
 * Copyright (C) 2011 - 2018 Vertispan LLC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.colinalworth.gwt.websockets.client.impl;

import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;
import com.colinalworth.gwt.websockets.shared.impl.ClientCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ClientInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerInvocation;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;

import java.util.HashMap;
import java.util.Map;

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

	private int nextCallbackId = 1;
	private Map<Integer, Callback<?,?>> callbacks = new HashMap<Integer, Callback<?, ?>>();

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
		connection = new WebSocket(url);
		connection.binaryType = "arraybuffer";

		connection.onopen = e -> {
			C client = __checkClient();
			if (client != null) {
				client.onOpen();
			}
		};
		connection.onclose = e -> {
			C client = __checkClient();
			if (client != null) {
				client.onClose();
			}
		};
		connection.onmessage = e -> {
			try {
				if (e.data instanceof String) {
					ServerImpl.this.__onMessage((String) e.data);
				} else {
					//arraybuffer
					//TODO
				}
			} catch (Exception ex) {
				if (getClient() != null) {
					// If getClient() returns null, the error is very likely "you need to assign a client to handle
					// messages", but oh well.
					getClient().onError(ex);
				} else if (errorHandler != null) {
					// might as well reuse this, since we apparently haven't finished connection enough to have a client
					errorHandler.onError(ex);
				} else {
					// fall back to GWT.log
					GWT.log("An error occurred while handling a message from the server, and no client or connection handler was found", ex);
				}
			}
		};
		connection.onerror = e -> {
			if (errorHandler != null) {
				errorHandler.onError(new JavaScriptException(e));
			} else {
				GWT.log("A transport error occurred - pass a error handler to your server builder to handle this yourself", new JavaScriptException(e));
			}
		};
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

		Object object = clientSerializationStreamReader.readObject();
		try {
			if (object instanceof ClientInvocation) {
				final ClientInvocation invocation = (ClientInvocation) object;
				if (invocation.getCallbackId() != 0) {
					Callback<?, ?> callback = new Callback<Object, Object>() {
						private boolean fired = false;
						@Override
						public void onFailure(Object reason) {
							checkFired();
							__sendCallback(invocation.getCallbackId(), reason, false);
						}

						@Override
						public void onSuccess(Object result) {
							checkFired();
							__sendCallback(invocation.getCallbackId(), result, true);
						}

						private void checkFired() {
							if (fired) {
								throw new IllegalStateException("Callback already used, cannot be used again.");
							}
							fired = true;
						}
					};
					//This is only legal in gwt'd java, where object arrays are backed by a js array
					invocation.getParameters()[invocation.getParameters().length] = callback;
				}
				__invoke(invocation.getMethod(), invocation.getParameters());
			} else {
				assert object instanceof ClientCallbackInvocation;
				ClientCallbackInvocation callback = (ClientCallbackInvocation) object;
				__callback(callback.getCallbackId(), callback.getResponse(), callback.isSuccess());
			}
		} catch (Exception ex) {
			//pass any error when invoking a client method back to the error handler
			getClient().onError(ex);
		}
	}

	private void __sendCallback(int callbackId, Object response, boolean isSuccess) {
		ServerCallbackInvocation callbackInvoke = new ServerCallbackInvocation(callbackId, response, isSuccess);

		AbstractSerializationStreamWriter writer = new ClientSerializationStreamWriter(__getSerializer(), "", "101010");
		// manually prepare to serialize a method call to keep the server side simpler
		writer.prepareToWrite();
		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.DummyRemoteService");//interface
		writer.writeString("callback");//method name
		writer.writeInt(1);//param count

		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.ServerCallbackInvocation");//param type

		try {
			// actually encode the object to send
			writer.writeObject(callbackInvoke);

			// send the message over the wire
			__getConnection().send(writer.toString());
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
	 * Serializes and send a message to the server based on a method invocation made.
	 *
	 * @param methodName
	 * @param params
	 */
	protected void __sendMessage(String methodName, Callback<?, ?> callback, Object... params) {
		final int callbackId;
		if (callback != null) {
			callbackId = nextCallbackId++;
			callbacks.put(callbackId, callback);
		} else {
			callbackId = 0;
		}
		ServerInvocation invoke = new ServerInvocation(methodName, params, callbackId);

		AbstractSerializationStreamWriter writer = new ClientSerializationStreamWriter(__getSerializer(), "", "101010");
		// manually prepare to serialize a method call to keep the server side simpler
		writer.prepareToWrite();
		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.DummyRemoteService");//interface
		writer.writeString("invoke");//method name
		writer.writeInt(1);//param count

		writer.writeString("com.colinalworth.gwt.websockets.shared.impl.ServerInvocation");//param type

		try {
			// actually encode the object to send
			writer.writeObject(invoke);

			// send the message over the wire
			__getConnection().send(writer.toString());
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

	private void __callback(int callbackId, Object response, boolean success) {
		if (callbacks.containsKey(callbackId)) {
			@SuppressWarnings("unchecked") Callback<Object, Object> c = (Callback<Object, Object>) callbacks.remove(callbackId);
			if (success) {
				c.onSuccess(response);
			} else {
				c.onFailure(response);
			}
		}
	}

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
