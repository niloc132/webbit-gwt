package com.colinalworth.gwt.websockets.shared.impl;

import com.google.gwt.user.client.rpc.IsSerializable;

public class ClientCallbackInvocation implements IsSerializable {
	private transient int callbackId;
	private transient Object response;
	private transient boolean isSuccess;

	public ClientCallbackInvocation() {
	}

	public ClientCallbackInvocation(int callbackId, Object response, boolean success) {
		this.callbackId = callbackId;
		this.response = response;
		isSuccess = success;
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

	public void setSuccess(boolean success) {
		isSuccess = success;
	}
}
