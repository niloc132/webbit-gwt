package sharedchat.common.shared;

import sharedchat.common.client.ChatPage;

/**
 * Created by colin on 2/7/17.
 */
public class ChatJoin implements ChatEvent {
	String username;

	public ChatJoin() {
	}

	public ChatJoin(String username) {
		this.username = username;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Override
	public void handle(ChatPage chatPage) {
		chatPage.join(getUsername());
	}
}
