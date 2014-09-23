package samples.easychatroom2.client;

import com.colinalworth.gwt.websockets.client.AbstractClientImpl;
import samples.easychatroom2.shared.ChatClient;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import samples.easychatroom2.shared.ChatServer;

public class ChatClientWidget extends AbstractClientImpl<ChatClient, ChatServer> implements ChatClient, IsWidget {
	FlowPanel panel = new FlowPanel();
	TextBox message = new TextBox();
	DockLayoutPanel root;
	Button send = new Button("Send");

	public ChatClientWidget() {
		root = new DockLayoutPanel(Unit.PX);

		HorizontalPanel input = new HorizontalPanel();
		message.setWidth("200px");
		input.add(message);
		input.add(send);

		root.addSouth(input, 30);
		ScrollPanel scroll = new ScrollPanel();
		scroll.add(panel);
		root.add(scroll);
	}

	@Override
	public Widget asWidget() {
		return root;
	}

	@Override
	public void say(String username, String message) {
		addMessage(username + ": " + message);
	}

	@Override
	public void join(String username) {
		addMessage(username + " has joined");
	}

	@Override
	public void part(String username) {
		addMessage(username + " has left");
	}

	@Override
	public void onOpen() {
		super.onOpen();
		addMessage("You've joined the chat");
	}

	@Override
	public void onClose() {
		super.onClose();
		addMessage("You've left the chat");
	}

	@Override
	public void onError(Throwable error) {
		Window.alert(error.getMessage());
	}

	protected void addMessage(String message) {
		Label m = new Label(message);
		panel.add(m);
		m.getElement().scrollIntoView();
	}

}
