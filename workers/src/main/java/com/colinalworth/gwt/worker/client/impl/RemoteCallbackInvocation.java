/*
 * #%L
 * workers
 * %%
 * Copyright (C) 2011 - 2018 Vertispan LLC
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
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
