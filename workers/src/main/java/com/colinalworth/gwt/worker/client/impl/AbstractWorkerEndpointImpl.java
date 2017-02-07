package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.Endpoint;
import com.colinalworth.gwt.worker.client.worker.MessageEvent;
import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.Serializer;
import playn.html.TypedArrayHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;

/**
 * Base implementation of Endpoint
 */
public abstract class AbstractWorkerEndpointImpl<LOCAL extends Endpoint<LOCAL, REMOTE>, REMOTE extends Endpoint<REMOTE, LOCAL>> implements Endpoint<LOCAL, REMOTE> {
	private final MessagePort worker;
	private REMOTE remote;

	private int nextCallbackId = 1;
	private Map<Integer, Callback<?,?>> callbacks = new HashMap<Integer, Callback<?, ?>>();


	protected AbstractWorkerEndpointImpl(MessagePort worker) {
		this.worker = worker;
		worker.addMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(MessageEvent event) {
				try {
					__onMessage(event);
				} catch (SerializationException e) {
					//TODO handle exception
				}
			}
		});
	}

	@Override
	public void setRemote(REMOTE remote) {
		this.remote = remote;
	}

	@Override
	public REMOTE getRemote() {
		return remote;
	}

	protected abstract Serializer __getSerializer();


	public void __onMessage(MessageEvent message) throws SerializationException {
		__checkLocal();
		//message is two parts, payload and strings
		JsArrayMixed data = message.getData();
		ByteBuffer byteBuffer = TypedArrayHelper.wrap(data.getObject(0));
		byteBuffer.order(ByteOrder.nativeOrder());
		JsArrayString strings = data.getObject(1);
		StreamReader reader = new StreamReader(__getSerializer(), byteBuffer, strings);

		Object object = reader.readObject();

		try {
			if (object instanceof RemoteInvocation) {
				final RemoteInvocation invocation = (RemoteInvocation) object;
				//TODO handle passed callback
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


				//TODO handle responding callback
			} else {
				assert object instanceof RemoteCallbackInvocation;
				RemoteCallbackInvocation callback = (RemoteCallbackInvocation) object;
				__callback(callback.getCallbackId(), callback.getResponse(), callback.isSuccess());

			}
		} catch (Exception ex) {
			//TODO handle exception, do not rethrow
		}
	}

	private void __sendCallback(int callbackId, Object response, boolean isSuccess) {
		RemoteCallbackInvocation callbackInvoke = new RemoteCallbackInvocation(callbackId, response, isSuccess);

		StreamWriter writer = new StreamWriter(__getSerializer());

		try {
			writer.writeObject(callbackInvoke);

			JsArrayMixed workerData = writer.getWorkerData();
			worker.postMessage(workerData, new ArrayBuffer[]{workerData.getObject(0)});
		} catch (SerializationException e) {
			//TODO report, rethrow
		}
	}

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

	protected void __sendMessage(String methodName, Callback<?, ?> callback, Object... params) {
		final int callbackId;
		if (callback != null) {
			callbackId = nextCallbackId++;
			callbacks.put(callbackId, callback);
		} else {
			callbackId = 0;
		}
		RemoteInvocation invoke = new RemoteInvocation(methodName, params, callbackId);

		StreamWriter writer = new StreamWriter(__getSerializer());

		try {
			writer.writeObject(invoke);

			JsArrayMixed workerData = writer.getWorkerData();
			worker.postMessage(workerData, new ArrayBuffer[]{workerData.getObject(0)});
		} catch (SerializationException e) {
			//TODO report, rethrow
		}

	}

	protected abstract void __invoke(String methodName, Object[] parameters);

	private void __checkLocal() {

	}
}

