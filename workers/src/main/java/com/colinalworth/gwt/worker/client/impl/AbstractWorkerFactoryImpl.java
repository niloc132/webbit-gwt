package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.Endpoint;
import com.colinalworth.gwt.worker.client.WorkerFactory;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.colinalworth.gwt.worker.client.worker.ServiceWorkerRegistration;
import com.colinalworth.gwt.worker.client.worker.SharedWorker;
import com.colinalworth.gwt.worker.client.worker.Worker;

/**
 * base class for generated factories, with a hook to create the remote endpoint to connect to
 */
public abstract class AbstractWorkerFactoryImpl<R extends Endpoint<R, L>, L extends Endpoint<L, R>> implements WorkerFactory<R, L> {

	@Override
	public R createDedicatedWorker(String pathToJs, L local) {

		Worker worker = new Worker(pathToJs);

		R remote = create(worker);

		remote.setRemote(local);
		local.setRemote(remote);

		return remote;
	}

	@Override
	public R createSharedWorker(String pathToJs, L local) {
		SharedWorker worker = new SharedWorker(pathToJs, pathToJs);
		R remote = create(worker.getPort());

		remote.setRemote(local);
		local.setRemote(remote);

		return remote;
	}

	@Override
	public R wrapRemoteMessagePort(MessagePort remote, L local) {
		R r = create(remote);

		r.setRemote(local);
		local.setRemote(r);

		return r;
	}

	/**
	 * build the actual instance to connect
	 */
	protected abstract R create(MessagePort worker);
}
