package samples.easychatroom2.shared;

import com.colinalworth.gwt.websockets.shared.Client;
import com.google.gwt.core.client.Callback;

/**
 * Simple example of methods implemented by a GWT client that can be called from the server
 *
 */
public interface ChatClient extends Client<ChatClient, ChatServer> {
	/**
	 * Tells the client that a user posted a message to the chat room
	 * @param username the user who sent the message
	 * @param message the message the user sent
	 */
	void say(String username, String message);

	/**
	 * Indicates that a new user has entered the chat room
	 * @param username the user who logged in
	 */
	void join(String username);

	/**
	 * Indicates that a user has left the chat room
	 * @param username the user who left
	 */
	void part(String username);


	/**
	 * Test method to have the server send the client a message and get a response right away
	 * @param callback response that the client should call upon receipt of this method
	 */
	void ping(Callback<Void, Void> callback);
}
