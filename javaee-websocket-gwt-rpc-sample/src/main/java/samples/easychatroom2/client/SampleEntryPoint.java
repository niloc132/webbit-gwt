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
package samples.easychatroom2.client;

import com.colinalworth.gwt.websockets.client.ConnectionClosedEvent;
import com.colinalworth.gwt.websockets.client.ConnectionOpenedEvent;
import com.colinalworth.gwt.websockets.client.ConnectionOpenedEvent.ConnectionOpenedHandler;
import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.colinalworth.gwt.websockets.shared.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.vertispan.serial.streams.string.StringSerializationStreamReader;
import com.vertispan.serial.streams.string.StringSerializationStreamWriter;
import elemental2.dom.WebSocket;
import samples.easychatroom2.shared.ChatServer;
import samples.easychatroom2.shared.ChatServer_Impl;

public class SampleEntryPoint implements EntryPoint {
	interface ChatServerBuilder extends ServerBuilder<ChatServer> {}

	@Override
	public void onModuleLoad() {
		// We can't GWT.create the server itself, instead, we need a builder
		//final ChatServer server = GWT.create(ChatServer.class);
		final ChatServerBuilder builder = GWT.create(ChatServerBuilder.class);

		// Set the url directly, or use the setHost, setPort, etc calls and
		//use the @RemoteServiceRelativePath given on the interface
//		builder.setUrl("ws://" + Window.Location.getHost() + "/chat");
//		builder.setHostname(Window.Location.getHostName());
		builder.setPath("chat");

		// Because this is just a demo, we're using Window.prompt to get a username
		final String username = Window.prompt("Select a username", "");

		// Start up the server connection, then plug into it so you get the callbacks
		final ChatServer server = builder.start();

		final ChatClientWidget impl = new ChatClientWidget();
		server.setClient(impl);


		//sample of how the websocket impl could be wired up, entirely generically
		WebSocket websocket = new WebSocket("ws://localhost");
		new ChatServer_Impl(
				ts -> new StringSerializationStreamWriter(ts, "", ""),
				writer -> websocket.send(writer.toString()),
				(thingie, typeSerializer) -> {
					websocket.onmessage = message -> {
						thingie.accept(new StringSerializationStreamReader(typeSerializer, message.data.toString()));
						return null;
					};
				}
		);

		// This listens for the connection to start, so we can log in with the username
		// we already picked.
		// Remember that you don't need to build this here, it could be in your client
		// impl's onOpen method (and the next block in onClose).
		impl.addConnectionOpenedHandler(new ConnectionOpenedHandler() {
			@Override
			public void onConnectionOpened(ConnectionOpenedEvent event) {
				server.login(username, new Callback<Void, String>() {
					@Override
					public void onFailure(String reason) {
						Window.alert(reason);
						final String username = Window.prompt("Select a username", "");
						server.login(username, this);
					}

					@Override
					public void onSuccess(Void result) {
						RootLayoutPanel.get().add(impl);
					}
				});
			}
		});

		// Then listen for close too, so we can do something about the lost connection.
		impl.addConnectionClosedHandler(new ConnectionClosedEvent.ConnectionClosedHandler() {
			@Override
			public void onConnectionClosed(ConnectionClosedEvent event) {
				// Take the easy/stupid way out and restart the page!
				// This will stop at the prompt and wait for a username before connecting
				Window.Location.reload();
			}
		});

		impl.send.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				server.say(impl.message.getValue());
				impl.message.setValue("");
			}
		});
	}

}
