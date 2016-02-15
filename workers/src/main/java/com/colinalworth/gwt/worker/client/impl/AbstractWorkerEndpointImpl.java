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

/**
 * Base implementation of Endpoint
 */
public abstract class AbstractWorkerEndpointImpl<LOCAL extends Endpoint<LOCAL, REMOTE>, REMOTE extends Endpoint<REMOTE, LOCAL>> implements Endpoint<LOCAL, REMOTE> {
	private final MessagePort worker;
	private REMOTE remote;

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
		ArrayBuffer payload = (ArrayBuffer) data.getObject(0);
		ByteBuffer byteBuffer = TypedArrayHelper.wrap(payload);
		byteBuffer.order(ByteOrder.nativeOrder());
		JsArrayString strings = data.getObject(1);
		StreamReader reader = new StreamReader(__getSerializer(), byteBuffer.asIntBuffer(), strings);

		Object object = reader.readObject();

		try {
			if (object instanceof RemoteInvocation) {
				RemoteInvocation invocation = (RemoteInvocation) object;
				//TODO handle passed callback

				__invoke(invocation.getMethod(), invocation.getParameters());


				//TODO handle responding callback
//			} else {
//				assert object instanceof RemoteCallbackInvocation;
//				RemoteCallbackInvocation callback = (RemoteCallbackInvocation) object;
//				__callback(callback.getCallbackId(), callback.getResponse(), callback.isSuccess());

			}
		} catch (Exception ex) {
			//TODO handle exception, do not rethrow
		}
	}

	protected void __sendMessage(String methodName, Callback<?, ?> callback, Object... params) {

		RemoteInvocation invoke = new RemoteInvocation(methodName, params, 0);

		StreamWriter writer = new StreamWriter(__getSerializer());

		try {
			writer.writeObject(invoke);

			JsArrayMixed workerData = writer.getWorkerData();
			ArrayBuffer buffer = workerData.getObject(0);
			worker.postMessage(workerData, buffer);
		} catch (SerializationException e) {
			//TODO report, rethrow
		}

	}

	protected abstract void __invoke(String methodName, Object[] parameters);

	private void __checkLocal() {

	}
}

