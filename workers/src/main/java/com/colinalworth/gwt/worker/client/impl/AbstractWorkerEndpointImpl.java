/*
 * #%L
 * workers
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
package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.Endpoint;
import com.colinalworth.gwt.worker.client.worker.MessageEvent;
import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.user.client.rpc.SerializationException;
import com.vertispan.serial.TypeSerializer;
import com.vertispan.serial.streams.bytebuffer.ByteBufferSerializationStreamReader;
import com.vertispan.serial.streams.bytebuffer.ByteBufferSerializationStreamWriter;
import elemental2.core.ArrayBuffer;
import elemental2.core.JsArray;
import elemental2.core.JsString;
import jsinterop.base.Any;
import jsinterop.base.Js;
import playn.html.HasArrayBufferView;
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
		worker.start();
	}

	@Override
	public void setRemote(REMOTE remote) {
		this.remote = remote;
	}

	@Override
	public REMOTE getRemote() {
		return remote;
	}

	protected abstract TypeSerializer __getSerializer();


	public void __onMessage(MessageEvent message) throws SerializationException {
		__checkLocal();
		//message is two parts, payload and strings
		JsArray<Any> data = message.getData();
		ByteBuffer byteBuffer = TypedArrayHelper.wrap(data.getAt(0).cast());
		byteBuffer.order(ByteOrder.nativeOrder());
		String[] strings = data.getAt(1).cast();
		ByteBufferSerializationStreamReader reader = new ByteBufferSerializationStreamReader(__getSerializer(), byteBuffer, strings);

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

		ByteBufferSerializationStreamWriter writer = new ByteBufferSerializationStreamWriter(__getSerializer());

		try {
			writer.writeObject(callbackInvoke);

			JsString[] stringTable = Js.<JsArray<JsString>>uncheckedCast(writer.getFinishedStringTable()).slice();
			ArrayBuffer payload = Js.cast(((HasArrayBufferView) writer.getPayloadBytes()).getTypedArray().buffer());

			worker.postMessage(new JsArray<>(payload, stringTable), new JsArray<>(payload));
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

		ByteBufferSerializationStreamWriter writer = new ByteBufferSerializationStreamWriter(__getSerializer());

		try {
			writer.writeObject(invoke);

			JsString[] stringTable = Js.<JsArray<JsString>>uncheckedCast(writer.getFinishedStringTable()).slice();
			ArrayBuffer payload = Js.cast(((HasArrayBufferView) writer.getPayloadBytes()).getTypedArray().buffer());

			worker.postMessage(new JsArray<>(payload, stringTable), new JsArray<>(payload));
		} catch (SerializationException e) {
			//TODO report, rethrow
		}

	}

	protected abstract void __invoke(String methodName, Object[] parameters);

	private void __checkLocal() {

	}
}

