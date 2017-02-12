package sharedchat.common.shared;

import sharedchat.common.client.ChatPage;

/**
 * Created by colin on 2/7/17.
 */
public class ChatMessage implements ChatEvent {
	private String username;
	private String text;

	public ChatMessage() {
	}

	public ChatMessage(String username, String text) {
		this.username = username;
		this.text = text;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public void handle(ChatPage chatPage) {
		chatPage.say(getUsername(), getText());
	}
}
