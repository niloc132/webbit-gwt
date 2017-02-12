package sharedchat.server;

import com.colinalworth.gwt.websockets.server.AbstractServerImpl;
import sharedchat.common.shared.ChatClient;
import sharedchat.common.shared.ChatServer;

import javax.websocket.server.ServerEndpoint;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/chat")
public class ChatServerImpl extends AbstractServerImpl<ChatServer, ChatClient> implements ChatServer {
	private static final Map<ChatClient, String> loggedIn = new ConcurrentHashMap<>();

	public ChatServerImpl() {
		super(ChatClient.class);
	}

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
	public void login(String username) {
		System.out.println("login: " + username);

		ChatClient c = getClient();
		for (ChatClient connected : loggedIn.keySet()) {
			connected.join(username);
		}
		loggedIn.put(c, username);
	}

	@Override
	public void say(String message) {
		System.out.println("say: " + message);
		ChatClient c = getClient();
		String userName = loggedIn.get(c);

		for (ChatClient connected : loggedIn.keySet()) {
			connected.say(userName, message);
		}
	}

	@Override
	public void onError(Throwable error) {
		error.printStackTrace();
	}

}