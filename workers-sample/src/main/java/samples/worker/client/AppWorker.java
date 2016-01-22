package samples.worker.client;

import com.colinalworth.gwt.worker.client.worker.MessageEvent;
import com.colinalworth.gwt.worker.client.worker.MessageEvent.MessageHandler;
import com.colinalworth.gwt.worker.client.worker.MessagePort;

import com.google.gwt.core.client.EntryPoint;

/**
 * Created by colin on 1/21/16.
 */
public class AppWorker implements EntryPoint {
	@Override
	public void onModuleLoad() {
		self().addMessageHandler(new MessageHandler() {
			@Override
			public void onMessage(MessageEvent event) {
				self().postMessage("pong");
			}
		});
	}

	private native MessagePort self() /*-{
		return $wnd;
	}-*/;
}
