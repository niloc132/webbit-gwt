/*
 * #%L
 * javaee-websocket-gwt-rpc-sample
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
package samples.easychatroom2.shared;

import com.colinalworth.gwt.websockets.shared.Callback;
import com.colinalworth.gwt.websockets.shared.impl.AbstractEndpointImpl;
import com.google.gwt.user.client.rpc.SerializationException;
import com.vertispan.serial.SerializationStreamReader;
import com.vertispan.serial.SerializationStreamWriter;
import com.vertispan.serial.SerializationWiring;
import com.vertispan.serial.TypeSerializer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChatClient_Impl extends AbstractEndpointImpl implements ChatClient {
	private final ClientSerializers s;

	public ChatClient_Impl(Function<TypeSerializer, SerializationStreamWriter> writerFactory, Consumer<SerializationStreamWriter> send, BiConsumer<Consumer<SerializationStreamReader>, TypeSerializer> onMessage) {
		this(writerFactory, send, onMessage, new ClientSerializers_Impl());
	}
	private ChatClient_Impl(Function<TypeSerializer, SerializationStreamWriter> writerFactory, Consumer<SerializationStreamWriter> send, BiConsumer<Consumer<SerializationStreamReader>, TypeSerializer> onMessage, ClientSerializers serializers) {
		super(
				writerFactory,
				send,
				serializers.createSerializer(),
				onMessage
		);
		s = serializers;
	}

	@Override
	public void say(String username, String message) {
		__send(() -> {
			activeWriter.writeInt(0);//say(string,string) == 0, etc
			s.writeString(username, activeWriter);
			s.writeString(message, activeWriter);
		});
	}

	@Override
	public void join(String username) {
		__send(() -> {
			activeWriter.writeInt(1);
			s.writeString(username, activeWriter);
		});
	}


	@Override
	public void part(String username) {
		__send(() -> {
			activeWriter.writeInt(2);
			s.writeString(username, activeWriter);
		});
	}

	@Override
	public void ping(Callback<Void, Void> callback) {
		__send(() -> {
			activeWriter.writeInt(3);

		}, new ReadingCallback<Void, Void>() {
			@Override
			public void success(SerializationStreamReader reader) {
				callback.onSuccess(null);
			}
			@Override
			public void failure(SerializationStreamReader reader) {
				callback.onFailure(null);
			}
		});
	}

	@Override
	protected void __invoke(int recipient, SerializationStreamReader reader) throws SerializationException {
		switch (recipient) {
			case 0: {
				// read callbackId first
				int callbackId = reader.readInt();
				getServer().login(
						s.readString(reader),
						new Callback<Void, String>() {
							@Override
							public void onSuccess(Void value) {
								__send(() -> {
									// indicate the callback in use
									activeWriter.writeInt(-callbackId);
									//write the value (void, so do nothing)
									//Void is skipped
								});
							}

							@Override
							public void onFailure(String error) {
								__send(() -> {
									// indicate the callback in use
									activeWriter.writeInt(-callbackId);
									//write the error
									s.writeString(error, activeWriter);
								});

							}
						});
				break;
			}
			case 1: {
				getServer().say(
						s.readString(reader)
				);
				break;
			}
		}
	}

	@Override
	protected void __onError(Throwable ex) {
		getServer().onError(ex);
	}

	@Override
	public void setServer(ChatServer server) {

	}

	@Override
	public ChatServer getServer() {
		return null;
	}

	@Override
	public void onOpen() {

	}

	@Override
	public void onClose() {

	}

	@Override
	public void onError(Throwable error) {

	}

	@SerializationWiring
	public interface ClientSerializers {
		TypeSerializer createSerializer();

		void writeString(String obj, SerializationStreamWriter writer);

		String readString(SerializationStreamReader reader);
	}
}
