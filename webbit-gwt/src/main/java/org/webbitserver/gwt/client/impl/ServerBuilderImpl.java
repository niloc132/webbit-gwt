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
package org.webbitserver.gwt.client.impl;

import org.webbitserver.gwt.client.ServerBuilder;
import org.webbitserver.gwt.shared.Server;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

/**
 * @author colin
 *
 */
public abstract class ServerBuilderImpl<S extends Server<S, ?>> implements ServerBuilder<S> {
	private String url;
	private UrlBuilder urlBuilder = Window.Location.createUrlBuilder();

	/**
	 * 
	 */
	public ServerBuilderImpl() {
		urlBuilder.setProtocol("https".equals(Window.Location.getProtocol()) ? "wss": "ws");
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
