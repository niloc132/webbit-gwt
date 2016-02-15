package com.colinalworth.gwt.worker.client;

import com.colinalworth.gwt.worker.client.worker.MessagePort;

/**
 * @todo how to differentiate between shared, dedicated, service workers...
 */
public interface WorkerFactory<R extends Endpoint<R, L>, L extends Endpoint<L, R>> {

	/**
	 * Creates a worker with the remote JS connected to the local endpoint.
	 * @param pathToJs
	 * @param local
	 * @return
	 */
	R createDedicatedWorker(String pathToJs, L local);

	R createSharedWorker(String pathToJs, L local);

	R wrapRemoteMessagePort(MessagePort remote, L local);
}
