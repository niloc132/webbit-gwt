package sharedchat.common.shared;

import sharedchat.common.client.ChatPage;

import java.io.Serializable;

/**
 * Created by colin on 2/7/17.
 */
public interface ChatEvent extends Serializable {
	void handle(ChatPage chatPage);
}
