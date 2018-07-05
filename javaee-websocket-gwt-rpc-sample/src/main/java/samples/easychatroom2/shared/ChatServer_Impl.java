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
import com.vertispan.serial.SerializationException;
import com.vertispan.serial.SerializationStreamReader;
import com.vertispan.serial.SerializationStreamWriter;
import com.vertispan.serial.SerializationWiring;
import com.vertispan.serial.TypeSerializer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class ChatServer_Impl extends AbstractEndpointImpl implements ChatServer {
	private final ServerSerializers s;

	private ChatClient client;

	public <S extends SerializationStreamWriter> ChatServer_Impl(
			Function<TypeSerializer, S> writerFactory,
			Consumer<S> send,
			BiConsumer<Consumer<SerializationStreamReader>, TypeSerializer> onMessage
	) {
		this(writerFactory, send, new ServerSerializers_Impl(), onMessage);
	}
	private <S extends SerializationStreamWriter> ChatServer_Impl(
			Function<TypeSerializer, S> writerFactory,
			Consumer<S> send,
			ServerSerializers serializers,
			BiConsumer<Consumer<SerializationStreamReader>, TypeSerializer> onMessage
	) {
		super(
				writerFactory,
				send,
				serializers.createSerializer(),
				onMessage
		);
		s = serializers;
	}
	@Override
	public void login(String username, Callback<Void, String> callback) {
		__send(() -> {
			activeWriter.writeInt(0);
			s.writeString(username, activeWriter);
		}, new ReadingCallback<Void, String>() {
			@Override
			public void success(SerializationStreamReader reader) throws SerializationException {
				callback.onSuccess(null);
			}

			@Override
			public void failure(SerializationStreamReader reader) throws SerializationException {
				callback.onFailure(s.readString(reader));
			}
		});
	}

	@Override
	public void say(String message) {
		__send(() -> {
			activeWriter.writeInt(1);
			s.writeString(message, activeWriter);
		});
	}

	@Override
	protected void __onError(Throwable ex) {
		getClient().onError(ex);
	}

	@Override
	protected void __invoke(int recipient, SerializationStreamReader reader) throws com.google.gwt.user.client.rpc.SerializationException {
		switch (recipient) {
			case 0: {
				getClient().say(
						s.readString(reader),
						s.readString(reader)
				);
				break;
			}
			case 1: {
				getClient().join(
						s.readString(reader)
				);
				break;
			}
			case 2: {
				getClient().part(
						s.readString(reader)
				);
				break;
			}
			case 3: {
				// read callbackId first
				int callbackId = reader.readInt();
				getClient().ping(
						new Callback<Void, Void>() {
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
							public void onFailure(Void error) {
								__send(() -> {
									// indicate the callback in use
									activeWriter.writeInt(-callbackId);
									//write the error (void, so do nothing)
									//Void is skipped
								});
							}
						}
				);
				break;
			}
		}
	}




	// probably group these up, put in a shared abstract class?
	@Override
	public void onOpen(Connection connection, ChatClient client) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}

	@Override
	public void onClose(Connection connection, ChatClient client) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}

	@Override
	public void onError(Throwable error) {
		throw new UnsupportedOperationException("Cannot be called from client code");
	}

	@Override
	public ChatClient getClient() {
		return client;
	}

	@Override
	public void setClient(ChatClient client) {
		this.client = client;
	}

	@Override
	public void close() {
		//TODO wire this up
	}




	// generated interface which will create our serializers
	@SerializationWiring
	public interface ServerSerializers {
		TypeSerializer createSerializer();

		void writeString(String obj, SerializationStreamWriter writer);

		String readString(SerializationStreamReader reader);
	}
}
