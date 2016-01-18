//package com.colinalworth.gwt.worker.client;
//
//import com.google.gwt.core.client.Callback;
//import com.google.gwt.junit.client.GWTTestCase;
//
//import java.util.List;
//
///**
// * Created by colin on 1/14/16.
// */
//public class EndpointTest extends GWTTestCase {
//	@Override
//	public String getModuleName() {
//		return "com.colinalworth.gwt.worker.RpcToWorkers";
//	}
//
//	public interface Host extends Endpoint<Host, Worker> {
//		void ping();
//	}
//	public interface Worker extends Endpoint<Worker, Host> {
//		void pong();
//
//		void split(String input, String pattern, Callback<List<String>, Throwable> callback);
//	}
//
//}