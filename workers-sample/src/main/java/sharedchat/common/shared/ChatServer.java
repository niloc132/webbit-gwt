package sharedchat.common.shared;

import com.colinalworth.gwt.websockets.shared.Server;
import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Created by colin on 2/7/17.
 */
@RemoteServiceRelativePath("chat")
public interface ChatServer extends Server<ChatServer, ChatClient> {
	/**
	 * Brings the user into the chat room, with the given username
	 * @param username the name to use
	 */
	void login(String username);

	/**
	 * Sends the given message to the chatroom
	 * @param message the message to say to the room
	 */
	void say(String message);
}
