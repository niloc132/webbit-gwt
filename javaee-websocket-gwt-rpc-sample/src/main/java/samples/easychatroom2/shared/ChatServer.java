package samples.easychatroom2.shared;

import com.colinalworth.gwt.websockets.shared.Server;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Simple example of methods a server can have that can be invoked by a client.
 *
 */
@RemoteServiceRelativePath("/chat")
public interface ChatServer extends Server<ChatServer, ChatClient> {
	/**
	 * Brings the user into the chat room, with the given username
	 * @param username the name to use
	 * @param callback indicates the login was successful, or passes back an error message
	 */
	void login(String username, Callback<Void, String> callback);

	/**
	 * Sends the given message to the chatroom
	 * @param message the message to say to the room
	 */
	void say(String message);
}
