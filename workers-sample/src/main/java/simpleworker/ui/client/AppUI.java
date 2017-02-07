package simpleworker.ui.client;

import com.colinalworth.gwt.worker.client.WorkerFactory;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import simpleworker.shared.client.MyHost;
import simpleworker.shared.client.MyWorker;

public class AppUI implements EntryPoint {
	public interface Factory extends WorkerFactory<MyWorker, MyHost> {}

	@Override
	public void onModuleLoad() {
		Factory factory = GWT.create(Factory.class);//new GeneratedWorkerFactory();

		final MyWorker worker = factory.createDedicatedWorker(GWT.getModuleBaseForStaticFiles() + "../worker/worker.js", new MyHost() {
			private MyWorker remote;

			@Override
			public void setRemote(MyWorker myWorker) {
				this.remote = myWorker;
			}

			@Override
			public MyWorker getRemote() {
				return remote;
			}

			@Override
			public void pong() {
				Window.alert("pong");
			}
		});


		RootPanel.get().add(new Button("Ping", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				worker.ping();
			}
		}));
		RootPanel.get().add(new Button("Split", new ClickHandler() {
			@Override
			public void onClick(ClickEvent clickEvent) {
				String result = Window.prompt("Split this text on \",\"", "a,b,c,d");
				if (result != null) {
					worker.split(",", result, new Callback<String[], Throwable>() {
						@Override
						public void onFailure(Throwable throwable) {
							Window.alert("failure: " + throwable.getMessage());
						}
						@Override
						public void onSuccess(String[] strings) {
							Window.alert(strings.length + " items");
						}
					});
				}
			}
		}));
	}

}
