package com.colinalworth.gwt.worker.client;

/**
 * @todo how to differentiate between shared, dedicated, service workers...
 */
public interface WorkerFactory<R extends Endpoint<R, ?>> {
	R createDedicatedWorker(String pathToJs);

}
