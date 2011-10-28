/**
 *  Copyright 2011 Colin Alworth
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.webbitserver.gwt.shared.impl;

import com.google.gwt.user.client.rpc.GwtTransient;
import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author colin
 *
 */
public class ClientInvocation implements IsSerializable {
	private String method;
	@GwtTransient
	private Object[] parameters;

	public ClientInvocation() {
	}
	public ClientInvocation(String method, Object[] params) {
		this.method = method;
		this.parameters = params;
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
