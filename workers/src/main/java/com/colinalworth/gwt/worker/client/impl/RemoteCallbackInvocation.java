package com.colinalworth.gwt.worker.client.impl;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by colin on 1/18/16.
 */
public class RemoteCallbackInvocation implements IsSerializable {
	private transient int callbackId;
	private transient Object response;
	private transient boolean isSuccess;

	public RemoteCallbackInvocation() {
	}

	public RemoteCallbackInvocation(int callbackId, Object response, boolean isSuccess) {
		this.callbackId = callbackId;
		this.response = response;
		this.isSuccess = isSuccess;
	}

	public int getCallbackId() {
		return callbackId;
	}

	public void setCallbackId(int callbackId) {
		this.callbackId = callbackId;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public boolean isSuccess() {
		return isSuccess;
	}

	public void setSuccess(boolean isSuccess) {
		this.isSuccess = isSuccess;
	}
}
