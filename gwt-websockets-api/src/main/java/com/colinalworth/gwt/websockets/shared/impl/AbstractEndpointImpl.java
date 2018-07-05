/*
 * #%L
 * gwt-websockets-api
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
package com.colinalworth.gwt.websockets.shared.impl;

import com.google.gwt.user.client.rpc.SerializationException;
import com.vertispan.serial.SerializationStreamReader;
import com.vertispan.serial.SerializationStreamWriter;
import com.vertispan.serial.TypeSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Base class for use in implementing any kind of endpoint, simplifying the code required to have in
 * generated concrete implementations.
 */
public abstract class AbstractEndpointImpl {
	private final Function<TypeSerializer, SerializationStreamWriter> writerFactory;
	private final Consumer<SerializationStreamWriter> send;

	private final TypeSerializer serializer;

	protected SerializationStreamWriter activeWriter;

	// count starts at 1, leaving zero for remote methods
	private int nextCallbackId = 1;
	private Map<Integer, ReadingCallback<?,?>> callbacks = new HashMap<>();


	protected <S extends SerializationStreamWriter> AbstractEndpointImpl(
			Function<TypeSerializer, S> writerFactory,
			Consumer<S> send,
			TypeSerializer serializer,
			BiConsumer<Consumer<SerializationStreamReader>, TypeSerializer> onMessage) {
		this.writerFactory = (Function<TypeSerializer, SerializationStreamWriter>) writerFactory;
		this.send = (Consumer<SerializationStreamWriter>) send;
		this.serializer = serializer;
		onMessage.accept(this::__onMessage, serializer);
	}

	/**
	 * Push any errors to the local (non-generated) endpoint implementation.
	 */
	protected abstract void __onError(Throwable ex);

	/**
	 * Invoke the remote invoked method on the local (non-generated) endpoint implementation.
	 */
	protected abstract void __invoke(int recipient, SerializationStreamReader reader) throws SerializationException;

	public void __onMessage(SerializationStreamReader reader) {
		try {
			int recipient = reader.readInt();
			if (recipient > 0) {
				__invoke(recipient, reader);
			} else {
				ReadingCallback<?, ?> callback = callbacks.get(-recipient);
				callback.handle(reader);
			}
		} catch (SerializationException ex) {
			__onError(ex);
		}
	}

	private void __startCall() {
		assert activeWriter == null;
		activeWriter = writerFactory.apply(serializer);
	}
	private void __endCall() {
		try {
			send.accept(activeWriter);
		} finally {
			activeWriter = null;
		}
	}

	/**
	 * Easy lambda to let generated classes run code which might throw within __send()
	 */
	protected interface Send {
		void send() throws SerializationException;
	}

	protected void __send(Send s) {
		__startCall();
		try {
			s.send();
			__endCall();
		} catch (SerializationException e) {
			__onError(e);
			throw new RuntimeException(e);
		} finally {
			activeWriter = null;
		}
	}
	protected void __send(Send s, ReadingCallback<?, ?> callback) {
		__startCall();
		try {
			int callbackId = nextCallbackId++;

			// add the callbackId to the message to send so the remote end knows it will need a callback
			// object when handling the rest of the body
			activeWriter.writeInt(callbackId);
			s.send();

			__endCall();
			//only after we've successfully sent, register the callback
			callbacks.put(callbackId, callback);
		} catch (SerializationException e) {
			//TODO report? can't actually pass to Callback.onFailure, since it might expect something else
			throw new RuntimeException(e);
		} finally {
			activeWriter = null;
		}
	}

	protected static abstract class ReadingCallback<T, F> {
		public final void handle(SerializationStreamReader reader) throws SerializationException {
			boolean success = reader.readBoolean();
			if (success) {
				success(reader);
			} else {
				failure(reader);
			}
		}
		public abstract void success(SerializationStreamReader reader) throws com.vertispan.serial.SerializationException;
		public abstract void failure(SerializationStreamReader reader) throws com.vertispan.serial.SerializationException;
	}
}
