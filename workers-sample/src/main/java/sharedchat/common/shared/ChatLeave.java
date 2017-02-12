package sharedchat.common.shared;

import sharedchat.common.client.ChatPage;

/**
 * Created by colin on 2/7/17.
 */
public class ChatLeave implements ChatEvent {
	private String username;

	public ChatLeave() {
	}

	public ChatLeave(String username) {
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
		chatPage.leave(getUsername());
	}
}
