/*
 * #%L
 * rpc-client-common
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
package com.colinalworth.gwt.websockets.client.impl;

import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.colinalworth.gwt.websockets.shared.Server;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

public abstract class ServerBuilderImpl<S extends Server<S, ?>> implements ServerBuilder<S> {
	private String url;
	private UrlBuilder urlBuilder = Window.Location.createUrlBuilder();
	private ConnectionErrorHandler errorHandler;

	/**
	 *
	 */
	public ServerBuilderImpl(String moduleBaseURL, String remoteServiceRelativePath) {
		urlBuilder.setProtocol(Window.Location.getProtocol().startsWith("https") ? "wss": "ws").setHash(null);

		//TODO in a worker moduleBaseURL can be null...
		if (remoteServiceRelativePath != null && moduleBaseURL != null) {

			//assume full url, pull out the path
			String basePath = moduleBaseURL.substring(moduleBaseURL.indexOf("/", moduleBaseURL.indexOf("://") + 3));
			/*
			 * If the module relative URL is not null we set the remote service URL to
			 * be the module base URL plus the module relative remote service URL.
			 * Otherwise an explicit call to
			 * ServerBuilder.setPath(String) or setUrl(String) is required.
			 */
			setPath(basePath + remoteServiceRelativePath);
		}

	}

	@Override
	public void setConnectionErrorHandler(ConnectionErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
	public ConnectionErrorHandler getErrorHandler() {
		return errorHandler;
	}

	@Override
	public ServerBuilder<S> setUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url == null ? urlBuilder.buildString() : url;
	}

	@Override
	public ServerBuilder<S> setProtocol(String protocol) {
		urlBuilder.setProtocol(protocol);
		return this;
	}
	@Override
	public ServerBuilder<S> setHostname(String hostname) {
		urlBuilder.setHost(hostname);
		return this;
	}
	@Override
	public ServerBuilder<S> setPort(int port) {
		urlBuilder.setPort(port);
		return this;
	}
	@Override
	public ServerBuilder<S> setPath(String path) {
		urlBuilder.setPath(path);
		return this;
	}
}