package com.colinalworth.gwt.websockets.client.impl;

import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.colinalworth.gwt.websockets.shared.Server;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.Window;

public abstract class ServerBuilderImpl<S extends Server<S, ?>> implements ServerBuilder<S> {
	private String url;
	private UrlBuilder urlBuilder;
	private ConnectionErrorHandler errorHandler;

	/**
	 *
	 */
	public ServerBuilderImpl() {
		urlBuilder = new UrlBuilder();
		urlBuilder.setProtocol("https".equals(Window.Location.getProtocol()) ? "wss": "ws");
      		urlBuilder.setHost(Window.Location.getHost());
		urlBuilder.setPath(Window.Location.getPath());
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
