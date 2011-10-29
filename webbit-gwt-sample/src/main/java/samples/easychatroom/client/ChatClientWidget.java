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

import org.webbitserver.gwt.client.AbstractClientImpl;

import samples.easychatroom.shared.ChatClient;
import samples.easychatroom.shared.ChatServer;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class ChatClientWidget extends AbstractClientImpl<ChatClient, ChatServer> implements ChatClient, IsWidget {
	FlowPanel panel = new FlowPanel();
	TextBox message = new TextBox();
	DockLayoutPanel root;
	Button send = new Button("Send");

	public ChatClientWidget() {
		root = new DockLayoutPanel(Unit.PX);

		VerticalPanel input = new VerticalPanel();
		input.add(message);
		input.add(send);

		root.addSouth(input, 30);
		root.add(panel);
	}

	@Override
	public Widget asWidget() {
		return root;
	}

	@Override
	public void say(String username, String message) {
		panel.add(new Label(username + ": " + message));
	}

	@Override
	public void join(String username) {
		panel.add(new Label(username + " has joined"));
	}

	@Override
	public void part(String username) {
		panel.add(new Label(username + " has left"));
	}

}