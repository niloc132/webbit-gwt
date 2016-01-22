package samples.ui.client;

import com.colinalworth.gwt.worker.client.worker.MessageEvent;
import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.colinalworth.gwt.worker.client.worker.Worker;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;

public class AppUI implements EntryPoint {
	Worker worker;
	@Override
	public void onModuleLoad() {
		worker = Worker.child(GWT.getModuleBaseForStaticFiles() + "../worker/worker.js");

		RootPanel.get().add(new Button("Ping", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				worker.postMessage("ping");
			}
		}));

		worker.addMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(MessageEvent event) {
				Window.alert(event.getData().toString());
			}
		});
	}
}
