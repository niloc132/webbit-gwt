package simpleworker.common.client;

import com.colinalworth.gwt.worker.client.Endpoint;

/**
 * Created by colin on 2/10/16.
 */
public interface MyHost extends Endpoint<MyHost, MyWorker> {
	void pong();
}
