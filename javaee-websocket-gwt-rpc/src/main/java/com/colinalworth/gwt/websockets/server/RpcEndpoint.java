package com.colinalworth.gwt.websockets.server;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;
import com.colinalworth.gwt.websockets.shared.Server.Connection;
import com.colinalworth.gwt.websockets.shared.impl.ClientCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ClientInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerCallbackInvocation;
import com.colinalworth.gwt.websockets.shared.impl.ServerInvocation;
import com.colinalworth.gwt.websockets.shared.impl.DummyRemoteService;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.SerializationPolicyProvider;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RpcEndpoint<S extends Server<S, C>, C extends Client<C, S>> {
	private static final Method INVOKE_RPC_METHOD;
	private static final Method CALLBACK_RPC_METHOD;
	static {
		Method m1 = null, m2 = null;
		try {
			m1 = DummyRemoteService.class.getDeclaredMethod("invoke", ServerInvocation.class);
			m2 = DummyRemoteService.class.getDeclaredMethod("callback", ServerCallbackInvocation.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		INVOKE_RPC_METHOD = m1;
		CALLBACK_RPC_METHOD = m2;
	}

	private final S server;
	private final Class<C> clientType;

	private final Map<String, Method> cachedMethods = Collections.synchronizedMap(new HashMap<String, Method>());


	private final Map<Integer, Callback<?, ?>> callbacks = Collections.synchronizedMap(new HashMap<Integer, Callback<?, ?>>());
	private final AtomicInteger nextCallbackId = new AtomicInteger(1);

	public RpcEndpoint(S server, Class<C> client) {
		assert server != null : "Can't make an endpoint with a null server instance";
		this.server = server;
		this.clientType = client;
	}

	//default accessible to let AbstractServerImpl call it
	RpcEndpoint(Class<C> client) {
		this.server = (S) this;
		this.clientType = client;
	}

	@OnOpen
	public void onOpen(final Session session) {
		assert server.getClient() == null;
		//make the client proxy, attach to session instance for future use
		//TODO
		C client = (C) Proxy.newProxyInstance(clientType.getClassLoader(), new Class<?>[]{clientType}, new IH(session));
		server.setClient(client);
		server.onOpen(new Jsr356Connection(session), server.getClient());

		//listen for future incoming calls (commented out since using annotated approach instead)
//    session.addMessageHandler(new MessageHandler.Whole<String>() {
//      @Override
//      public void onMessage(String message) {
//        handleMessage(session, message);
//      }
//    });
		session.setMaxIdleTimeout(0);
	}

	@OnMessage
	public void handleMessage(Session session, String message) {
		//TODO hopefully this isn't *actually* required for all containers, but seems needed for jetty 9
		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

		RPCRequest req = RPC.decodeRequest(message, null, makeProvider());
		//Method m = req.getMethod();//garbage

		Object object = req.getParameters()[0];

		if (object instanceof ServerCallbackInvocation) {
			ServerCallbackInvocation callbackInvoke = (ServerCallbackInvocation) object;

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

		ServerInvocation invoke = (ServerInvocation) object;
		//TODO verify the method to be invoked

		Method method;
		//TODO this caching probably doesn't make sense for jsr356 since we get a new instance per connection
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

		try {
			if (invoke.getCallbackId() != 0) {
				Object[] args = addCallbackToArgs(invoke.getParameters(), invoke.getCallbackId(), session);
				method.invoke(server, args);
			} else {
				method.invoke(server, invoke.getParameters());
			}
		} catch (InvocationTargetException ex) {
			Throwable unwrapped = ex.getCause();
			server.onError(unwrapped);
		} catch (IllegalAccessException e) {
			server.onError(e);
		}
	}

	private Object[] addCallbackToArgs(Object[] originalArgs, final int callbackId, final Session session) {
		Object[] newArgs = new Object[originalArgs.length + 1];
		System.arraycopy(originalArgs, 0, newArgs, 0, originalArgs.length);
		newArgs[originalArgs.length] = new Callback<Object, Object>() {
			boolean fired = false;
			@Override
			public void onSuccess(Object result) {
				checkFired();
				callback(callbackId, result, true, session);
			}

			@Override
			public void onFailure(Object reason) {
				checkFired();
				callback(callbackId, reason, false, session);
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

	private void callback(int callbackId, Object response, boolean isSuccess, Session session) {
		ClientCallbackInvocation callbackInvocation = new ClientCallbackInvocation(callbackId, response, isSuccess);

		try {
			// Encode and send the message
			int flags = 0;
			String message = RPC.encodeResponseForSuccess(CALLBACK_RPC_METHOD, callbackInvocation, makePolicy(), flags);
			session.getAsyncRemote().sendText(message);
		} catch (SerializationException e) {
			throw new RuntimeException("Failed to send callback to client, " + e);
		}

	}

	@OnClose
	public void onClose(Session session) {
		assert server.getClient() != null;
		server.onClose(new Jsr356Connection(session), server.getClient());
	}

	@OnError
	public void onError(Throwable thr) {
		if (server != this) {
			server.onError(thr);
		}
	}

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

	private class IH implements InvocationHandler {
		private final Session session;

		private IH(Session session) {
			this.session = session;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
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

			session.getAsyncRemote().sendText(message);
			//TODO handle future's error, if any


			//all methods are voids, so no return value
			return null;
		}
	}

	private static class Jsr356Connection implements Connection {
		private final Session session;

		private Jsr356Connection(Session session) {
			this.session = session;
		}

		@Override
		public void data(String key, Object value) {
			session.getUserProperties().put(key, value);
		}

		@Override
		public Object data(String key) {
			return session.getUserProperties().get(key);
		}

		@Override
		public void close() {
			try {
				session.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
