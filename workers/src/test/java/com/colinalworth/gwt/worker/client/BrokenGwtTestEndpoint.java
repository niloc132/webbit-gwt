package com.colinalworth.gwt.worker.client;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.junit.client.GWTTestCase;

import java.util.List;

/**
 * Created by colin on 1/14/16.
 */
public class BrokenGwtTestEndpoint extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "com.colinalworth.gwt.worker.RpcToWorkers";
	}

	public interface MyHost extends Endpoint<MyHost, MyWorker> {
		void ping();
	}
	public interface MyWorker extends Endpoint<MyWorker, MyHost> {
		void pong();

		void split(String input, String pattern, Callback<List<String>, Throwable> callback);
	}
	public interface MyWorkerFactory extends WorkerFactory<MyWorker> {
		MyWorkerFactory instance = GWT.create(WorkerFactory.class);
	}


	public void testSimpleEndpoint() {
		MyWorker worker = MyWorkerFactory.instance.createDedicatedWorker("simpleWorker.js");

		delayTestFinish(1000);

		worker.setLocal(new MyWorker() {
			@Override
			public void pong() {

			}

			@Override
			public void split(String input, String pattern, Callback<List<String>, Throwable> callback) {

			}

			@Override
			public void setLocal(MyWorker myWorker) {

			}
		});
	}
}