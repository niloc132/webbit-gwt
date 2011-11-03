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
package samples.easychatroom.client;

import org.webbitserver.gwt.client.ConnectionOpenedEvent;
import org.webbitserver.gwt.client.ConnectionOpenedEvent.ConnectionOpenedHandler;
import org.webbitserver.gwt.client.ServerBuilder;

import samples.easychatroom.shared.ChatServer;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootLayoutPanel;

/**
 * @author colin
 *
 */
public class SampleEntryPoint implements EntryPoint {
	interface ChatServerBuilder extends ServerBuilder<ChatServer> {}

	@Override
	public void onModuleLoad() {
		//final ChatServer server = GWT.create(ChatServer.class);
		final ChatServerBuilder builder = GWT.create(ChatServerBuilder.class);
		builder.setUrl("ws://" + Window.Location.getHost() + "/chat");

		final ChatServer server = builder.start();

		final ChatClientWidget impl = new ChatClientWidget();
		server.setClient(impl);

		impl.addConnectionOpenedHandler(new ConnectionOpenedHandler() {
			@Override
			public void onConnectionOpened(ConnectionOpenedEvent event) {
				String username = Window.prompt("Select a username", "");
				server.login(username);
			}
		});

		impl.send.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				server.say(impl.message.getValue());
				impl.message.setValue("");
			}
		});

		RootLayoutPanel.get().add(impl);
	}

}
