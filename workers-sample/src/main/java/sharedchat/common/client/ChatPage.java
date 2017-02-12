package sharedchat.common.client;

import com.colinalworth.gwt.worker.client.Endpoint;
import sharedchat.common.shared.ChatEvent;

import java.util.List;

/**
 * Created by colin on 2/6/17.
 */
public interface ChatPage extends Endpoint<ChatPage, ChatWorker> {
	void init(String username, List<ChatEvent> events);

	void connected();

	void disconnected();

	void join(String username);

	void leave(String username);

	void say(String username, String text);
}
