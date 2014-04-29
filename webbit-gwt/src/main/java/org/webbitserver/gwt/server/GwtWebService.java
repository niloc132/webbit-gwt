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

import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;
import org.webbitserver.gwt.shared.Client;
import org.webbitserver.gwt.shared.Server;
import org.webbitserver.gwt.shared.impl.ClientInvocation;
import org.webbitserver.gwt.shared.impl.ServerInvocation;
import org.webbitserver.gwt.shared.impl.WebbitService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Web Socket handler designed to talk to a GWT client, passing messages back and forth using the
 * RPC message format.
 * 
 * The two interfaces {@link Client} and {@link Server} are at the heart of this. User code must
 * create subinterfaces to declare the contract between the client and server, and must make the
 * relationship between these two clear using generics so both client and server code are able to
 * pass messages correctly.
 *
 */
public class GwtWebService<S extends Server<S,C>, C extends Client<C,S>> implements WebSocketHandler {
	private static final Method DUMMY_RPC_METHOD;
	static {
		Method m = null;
		try {
			m = WebbitService.class.getDeclaredMethod("dummy", ServerInvocation.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		DUMMY_RPC_METHOD = m;
	}

	private final S server;
	private final Class<C> client;

	private final Map<String, Method> cachedMethods = Collections.synchronizedMap(new HashMap<String, Method>());

	/**
	 * Creates a new service instance, making method calls to the given server instance, and 
	 * able to send method calls to the client of the given type.
	 * 
	 * @param server
	 * @param client
	 */
	public GwtWebService(S server, Class<C> client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void onOpen(WebSocketConnection connection) throws Exception {
		//
		C clientInstance = (C)Proxy.newProxyInstance(client.getClassLoader(), new Class<?>[] {client}, new IH(connection));
		connection.data(getClass().getName() + ".client", clientInstance);
		server.onOpen(connection, clientInstance);
	}

	@Override
	public void onClose(WebSocketConnection connection) throws Exception {
		C clientInstance = (C)connection.data(getClass().getName() + ".client");
		server.onClose(connection, clientInstance);
	}

	@Override
	public void onPing(WebSocketConnection webSocketConnection, byte[] bytes) throws Throwable {
		//no-op for now
	}

	@Override
	public void onPong(WebSocketConnection webSocketConnection, byte[] bytes) throws Throwable {
		//no-op for now
	}

	@Override
	public void onMessage(WebSocketConnection connection, byte[] msg) throws Throwable {
		//no-op for uploading binary data
	}

	@Override
	public void onMessage(WebSocketConnection connection, String msg) throws Throwable {
		RPCRequest req = RPC.decodeRequest(msg, null, makeProvider());
		//Method m = req.getMethod();//garbage

		ServerInvocation invoke = (ServerInvocation)req.getParameters()[0];
		//TODO verify the method to be invoked

		Method method;
		synchronized (cachedMethods) {
			method = cachedMethods.get(invoke.getMethod());
			if (method == null) {
				for (Method m : server.getClass().getMethods()) {
					if (m.getName().equals(invoke.getMethod())) {
						//TODO check better than this, verify the method may be invoked from the client
						method = m;
						break;
					}
				}
				if (method == null) {
					//TODO throw something
				} else {
					cachedMethods.put(invoke.getMethod(), method);
				}
			}
		}

		server.setClient((C)connection.data(getClass().getName() + ".client"));
		try {
			method.invoke(server, invoke.getParameters());
		} catch (InvocationTargetException ex) {
			Throwable unwrapped = ex.getCause();
			server.onError(unwrapped);
		} finally {
			server.setClient(null);
		}
	}

	//TODO factor this out elsewhere, and replace with the real thing
	protected static SerializationPolicy makePolicy() {
		return new SerializationPolicy() {
			@Override
			public void validateSerialize(Class<?> clazz) throws SerializationException {
			}
			@Override
			public void validateDeserialize(Class<?> clazz)
			throws SerializationException {
			}
			@Override
			public boolean shouldSerializeFields(Class<?> clazz) {
				return clazz != null;
			}
			@Override
			public boolean shouldDeserializeFields(Class<?> clazz) {
				return clazz != null;
			}
		};
	}
	protected static SerializationPolicyProvider makeProvider() {
		return new SerializationPolicyProvider() {
			@Override
			public SerializationPolicy getSerializationPolicy(String moduleBaseURL,
					String serializationPolicyStrongName) {
				return makePolicy();
			}
		};
	}

	private static class IH implements InvocationHandler {
		private final WebSocketConnection connection;
		public IH(WebSocketConnection connection) {
			this.connection = connection;
		}
		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
		throws Throwable {
			// First, directly call methods defined on Object (is this even needed?)
			if (method.getDeclaringClass() == Object.class) {
				return method.invoke(this, args);
			}
			// Then, make sure the server isn't calling client bookkeeping methods
			if (method.getDeclaringClass() == Client.class) {
				throw new IllegalStateException("This method is only intended to be called on the client itself");
			}
			//TODO work this over ahead of time, dont make a runtime check
			if (method.getReturnType() != void.class && method.getReturnType() != Void.class) {
				throw new IllegalArgumentException("Calls to client must be of return type Void");
			}

			// Build an invocation instance to send to the client
			ClientInvocation invocation = new ClientInvocation(method.getName(), args);

			// Encode and send the message
			int flags = 0;
			String message = RPC.encodeResponseForSuccess(DUMMY_RPC_METHOD, invocation, makePolicy(), flags);
			connection.send(message);

			return null;//void method, so no return val
		}
	}

}
