package simpleworker.shared.client;

import com.colinalworth.gwt.worker.client.Endpoint;
import com.google.gwt.core.client.Callback;

/**
 * Created by colin on 2/10/16.
 */
public interface MyWorker extends Endpoint<MyWorker, MyHost> {
	void ping();

	void split(String pattern, String input, Callback<String[], Throwable> callback);
}
