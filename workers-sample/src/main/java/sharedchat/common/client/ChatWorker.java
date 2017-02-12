package sharedchat.common.client;

import com.colinalworth.gwt.worker.client.Endpoint;

/**
 * Created by colin on 2/6/17.
 */
public interface ChatWorker extends Endpoint<ChatWorker, ChatPage> {
	void login(String username);

	void logout();

	void send(String message);
}
