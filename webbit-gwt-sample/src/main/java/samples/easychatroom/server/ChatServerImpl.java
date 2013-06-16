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
package samples.easychatroom.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.webbitserver.WebSocketConnection;
import org.webbitserver.gwt.server.AbstractServerImpl;

import samples.easychatroom.shared.ChatClient;
import samples.easychatroom.shared.ChatServer;

/**
 * @author colin
 *
 */
public class ChatServerImpl extends AbstractServerImpl<ChatServer, ChatClient> implements ChatServer {
	private final Map<ChatClient, String> loggedIn = Collections.synchronizedMap(new HashMap<ChatClient, String>());

	@Override
	public void onClose(WebSocketConnection connection, ChatClient client) {
		String userName = loggedIn.remove(client);
		if (userName == null) {
			return;
		}

		for (ChatClient connected : loggedIn.keySet()) {
			connected.part(userName);
		}
	}

	@Override
	public void login(String username) {
		if (username == null || username.length() == 0) {
			throw new IllegalArgumentException("Non-empty username required");
		}

		ChatClient c = getClient();
		if (loggedIn.containsKey(c)) {
			throw new IllegalArgumentException("Already logged in");
		}
		for (String name : loggedIn.values()) {
			if (name.equals(username)) {
				throw new IllegalArgumentException("Username already in use");
			}
		}
		for (ChatClient connected : loggedIn.keySet()) {
			connected.join(username);
		}
		loggedIn.put(c, username);
	}

	@Override
	public void say(String message) {
		ChatClient c = getClient();
		String userName = loggedIn.get(c);

		for (ChatClient connected : loggedIn.keySet()) {
			connected.say(userName, message);
		}
	}

}
