package samples.shared.client;

import com.colinalworth.gwt.worker.client.Endpoint;

/**
 * Created by colin on 2/10/16.
 */
public interface MyWorker extends Endpoint<MyWorker, MyHost> {
	void ping();
}
