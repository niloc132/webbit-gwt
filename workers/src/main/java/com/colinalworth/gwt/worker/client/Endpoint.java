package com.colinalworth.gwt.worker.client;

/**
 * Created by colin on 1/14/16.
 */
public interface Endpoint<LOCAL extends Endpoint<LOCAL, REMOTE>, REMOTE extends Endpoint<REMOTE, LOCAL>> {

	void setLocal(LOCAL local);
}
