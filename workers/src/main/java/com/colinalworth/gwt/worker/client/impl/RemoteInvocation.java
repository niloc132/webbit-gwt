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

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by colin on 1/18/16.
 */
public class RemoteInvocation implements IsSerializable {
	private String method;
	@GwtTransient
	private Object[] parameters;

	private int callbackId;

	public RemoteInvocation() {
	}

	public RemoteInvocation(String method, Object[] params, int callbackId) {
		this.method = method;
		this.parameters = params;
		this.callbackId = callbackId;
	}

	/**
	 * Returns the id of the callback on the client that should be invoked when the callback argument is used on the server
	 * @return a non-zero value if a callback should be invoked
	 */
	public int getCallbackId() {
		return callbackId;
	}

	public void setCallbackId(int callbackId) {
		this.callbackId = callbackId;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}
	/**
	 * Package-protected to keep it easy to call by custom serializers
	 * @param method
	 */
	void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the parameters
	 */
	public Object[] getParameters() {
		return parameters;
	}

	/**
	 * Package-protected to keep it easy to call by custom serializers
	 * @param parameters
	 */
	void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
}
