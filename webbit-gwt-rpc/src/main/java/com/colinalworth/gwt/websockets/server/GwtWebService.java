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
package com.colinalworth.gwt.websockets.server;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;
import com.colinalworth.gwt.websockets.shared.Server.Connection;
import com.colinalworth.gwt.websockets.shared.impl.ClientCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ClientInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerInvocation;
import com.colinalworth.gwt.websockets.shared.impl.WebbitService;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Web Socket handler designed to talk to a GWT client, passing messages back and forth using the
 * RPC message format.
 * 
 * The two interfaces {@link Client} and {@link Server} are at the heart of this. User code must
 * create subinterfaces to declare the contract between the client and server, and must make the
 * relationship between these two clear using generics so both client and server code are able to
 * pass messages correctly.
 *
 * {@link WebSocketHandler} objects are not unlike servlets - one instance handles all traffic. This
 * is distinct from how the servlet spec handles websockets, so in this implementation, the specific
 * client instance is tracked in the connection object itself.
 *
 */
public class GwtWebService<S extends Server<S,C>, C extends Client<C,S>> implements WebSocketHandler {
	private static final Method INVOKE_RPC_METHOD;
	private static final Method CALLBACK_RPC_METHOD;
	static {
		Method m1 = null, m2 = null;
		try {
			m1 = WebbitService.class.getDeclaredMethod("invoke", ServerInvocation.class);
			m2 = WebbitService.class.getDeclaredMethod("callback", ServerCallbackInvocation.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		INVOKE_RPC_METHOD = m1;
		CALLBACK_RPC_METHOD = m2;
	}

	private final S server;
	private final Class<C> client;

	private final Map<String, Method> cachedMethods = Collections.synchronizedMap(new HashMap<String, Method>());

	private final Map<Integer, Callback<?, ?>> callbacks = Collections.synchronizedMap(new HashMap<Integer, Callback<?, ?>>());
	private final AtomicInteger nextCallbackId = new AtomicInteger(1);

	/**
	 * Creates a new service instance, making method calls to the given server instance, and 
	 * able to send method calls to the client of the given type.
	 * 
	 * @param server an instance of the server interface to receive calls from the client
	 * @param client the type of the client to provide access to on the far end of this connection
	 */
	public GwtWebService(S server, Class<C> client) {
		this.server = server;
		this.client = client;
	}

	@Override
	public void onOpen(WebSocketConnection connection) throws Exception {
		//
		C clientInstance = (C)Proxy.newProxyInstance(client.getClassLoader(), new Class<?>[] {client}, new IH(connection, callbacks, nextCallbackId));
		connection.data(getClass().getName() + ".client", clientInstance);
		server.onOpen(new WebbitConnection(connection), clientInstance);
	}

	@Override
	public void onClose(WebSocketConnection connection) throws Exception {
		C clientInstance = (C)connection.data(getClass().getName() + ".client");
		server.onClose(new WebbitConnection(connection), clientInstance);
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

		Object message = req.getParameters()[0];

		if (message instanceof ServerCallbackInvocation) {
			ServerCallbackInvocation callbackInvoke = (ServerCallbackInvocation) message;

			if (callbacks.containsKey(callbackInvoke.getCallbackId())) {
				@SuppressWarnings("unchecked")
				Callback<Object, Object> callback = (Callback<Object, Object>) callbacks.remove(callbackInvoke.getCallbackId());

				if (callbackInvoke.isSuccess()) {
					callback.onSuccess(callbackInvoke.getResponse());
				} else {
					callback.onFailure(callbackInvoke.getResponse());
				}
			}
			return;
		}

		ServerInvocation invoke = (ServerInvocation) message;

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
			if (invoke.getCallbackId() != 0) {
				Object[] args = addCallbackToArgs(invoke.getParameters(), invoke.getCallbackId(), connection);
				method.invoke(server, args);
			} else {
				method.invoke(server, invoke.getParameters());
			}
		} catch (InvocationTargetException ex) {
			Throwable unwrapped = ex.getCause();
			server.onError(unwrapped);
		} finally {
			server.setClient(null);
		}
	}

	private Object[] addCallbackToArgs(Object[] originalArgs, final int callbackId, final WebSocketConnection connection) {
		Object[] newArgs = new Object[originalArgs.length + 1];
		System.arraycopy(originalArgs, 0, newArgs, 0, originalArgs.length);
		newArgs[originalArgs.length] = new Callback<Object, Object>() {
			boolean fired = false;
			@Override
			public void onSuccess(Object result) {
				checkFired();
				callback(callbackId, result, true, connection);
			}

			@Override
			public void onFailure(Object reason) {
				checkFired();
				callback(callbackId, reason, false, connection);
			}

			private void checkFired() {
				if (fired) {
					throw new IllegalStateException("Callback already used, cannot be used again.");
				}
				fired = true;
			}
		};
		return newArgs;
	}

	private void callback(int callbackId, Object response, boolean isSuccess, WebSocketConnection connection) {
		ClientCallbackInvocation callbackInvocation = new ClientCallbackInvocation(callbackId, response, isSuccess);

		try {
			// Encode and send the message
			int flags = 0;
			String message = RPC.encodeResponseForSuccess(CALLBACK_RPC_METHOD, callbackInvocation, makePolicy(), flags);
		  connection.send(message);
		} catch (SerializationException e) {
			throw new RuntimeException("Failed to send callback to client, " + e);
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
			public boolean shouldSerializeFinalFields() {
				return true;
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
		private final Map<Integer, Callback<?, ?>> callbacks;
		private final AtomicInteger nextCallbackId;

		public IH(WebSocketConnection connection, Map<Integer, Callback<?, ?>> callbacks, AtomicInteger nextCallbackId) {
			this.connection = connection;
			this.callbacks = callbacks;
			this.nextCallbackId = nextCallbackId;
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

			final int callbackId;
			final Object[] actualArgs;
			if (method.getParameterTypes()[method.getParameterTypes().length - 1] == Callback.class) {
				callbackId = nextCallbackId.getAndIncrement();
				callbacks.put(callbackId, (Callback<?, ?>) args[args.length - 1]);
				actualArgs = new Object[args.length - 1];
				System.arraycopy(args, 0, actualArgs, 0, actualArgs.length);
			} else {
				callbackId = 0;
				actualArgs = args;
			}

			// Build an invocation instance to send to the client
			ClientInvocation invocation = new ClientInvocation(method.getName(), actualArgs, callbackId);

			// Encode and send the message
			int flags = 0;
			String message = RPC.encodeResponseForSuccess(INVOKE_RPC_METHOD, invocation, makePolicy(), flags);
			connection.send(message);

			return null;//void method, so no return val
		}
	}

  private static class WebbitConnection implements Connection {
    private final WebSocketConnection wrapped;

    private WebbitConnection(WebSocketConnection wrapped) {
      this.wrapped = wrapped;
    }

    @Override
    public void data(String key, Object value) {
      wrapped.data(key, value);
    }

    @Override
    public Object data(String key) {
      return wrapped.data(key);
    }

    @Override
    public void close() {
      wrapped.close();
    }
  }

}
