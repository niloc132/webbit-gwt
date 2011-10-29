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
package org.webbitserver.gwt.client.impl;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.gwt.shared.Client;
import org.webbitserver.gwt.shared.Server;
import org.webbitserver.gwt.shared.impl.ClientInvocation;
import org.webbitserver.gwt.shared.impl.ServerInvocation;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptException;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.ClientSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;

/**
 * Simple impl of what the client thinks of the server as able to do.
 *
 */
public abstract class ServerImpl<S extends Server<S,C>, C extends Client<C,S>> implements Server<S, C> {
	private C client;
	private final WebSocket connection;

	/**
	 * 
	 */
	public ServerImpl(String path) {
		String server = Window.Location.getHost();
		connection = WebSocket.create(server, path, new WebSocket.Callback() {
			@Override
			public void onOpen() {
				C client = __checkClient();
				if (client != null) {
					client.onOpen();
				}
			}
			@Override
			public void onClose() {
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
					ServerImpl.this.__onError(e);
				}
			}
			@Override
			public void onError(JavaScriptObject error) {
				ServerImpl.this.__onError(new JavaScriptException(error));
			}
		});
	}

	protected abstract Serializer __getSerializer();

	private void __onMessage(String message) throws SerializationException {
		__checkClient();
		assert message.startsWith("//OK");//consider axing this , and the substring below
		ClientSerializationStreamReader clientSerializationStreamReader = new ClientSerializationStreamReader(__getSerializer());
		clientSerializationStreamReader.prepareToRead(message.substring(4));

		ClientInvocation decodedMessage = (ClientInvocation) clientSerializationStreamReader.readObject();
		__invoke(decodedMessage.getMethod(), decodedMessage.getParameters());
	}

	protected void __sendMessage(String methodName, Object...params ) {
		ServerInvocation invoke = new ServerInvocation(methodName, params);

		AbstractSerializationStreamWriter writer = new ClientSerializationStreamWriter(__getSerializer(), "", "");
		writer.prepareToWrite();
		writer.writeString("org.webbitserver.gwt.shared.impl.WebbitService");
		writer.writeString("dummy");
		writer.writeInt(1);

		writer.writeString("org.webbitserver.gwt.shared.impl.ServerInvocation");

		try {
			writer.writeObject(invoke);
		} catch (SerializationException e) {
			__onError(e);
		}
		__getConnection().sendMessage(writer.toString());
	}

	protected abstract void __invoke(String method, Object[] params);
	protected abstract void __onError(Exception error);

	public final WebSocket __getConnection() {
		return connection;
	}

	@Override
	public final void setClient(C client) {
		//TODO open or close connection here?
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
	public final void onOpen(WebSocketConnection connection, C client) throws Exception {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
	@Override
	public final void onClose(WebSocketConnection connection, C client) throws Exception {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}
}
