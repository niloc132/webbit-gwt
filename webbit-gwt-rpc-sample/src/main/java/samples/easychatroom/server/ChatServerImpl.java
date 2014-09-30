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

import com.colinalworth.gwt.websockets.server.AbstractServerImpl;
import com.google.gwt.core.client.Callback;
import samples.easychatroom.shared.ChatClient;
import samples.easychatroom.shared.ChatServer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatServerImpl extends AbstractServerImpl<ChatServer, ChatClient> implements ChatServer {
	private final Map<ChatClient, String> loggedIn = Collections.synchronizedMap(new HashMap<ChatClient, String>());

	@Override
	public void onClose(Connection connection, ChatClient client) {
		String userName = loggedIn.remove(client);
		if (userName == null) {
			return;
		}

		for (ChatClient connected : loggedIn.keySet()) {
			connected.part(userName);
		}
	}

	@Override
	public void login(String username, Callback<Void, String> callback) {
		if (username == null || username.length() == 0) {
			callback.onFailure("Non-empty username required");
		}

		ChatClient c = getClient();
		if (loggedIn.containsKey(c)) {
			callback.onFailure("Already logged in");
		}
		for (String name : loggedIn.values()) {
			if (name.equals(username)) {
				callback.onFailure("Username already in use");
			}
		}
		for (ChatClient connected : loggedIn.keySet()) {
			connected.join(username);
		}
		loggedIn.put(c, username);
		callback.onSuccess(null);

		final long start = System.nanoTime();
		getClient().ping(new Callback<Void, Void>() {
			@Override
			public void onFailure(Void reason) {
				System.err.println("failed login ping");
			}

			@Override
			public void onSuccess(Void result) {
				System.out.println("login ping in " + (System.nanoTime() - start) / 1000000.0 + "milliseconds");
			}
		});
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
