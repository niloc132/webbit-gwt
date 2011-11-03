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
package org.webbitserver.gwt.client;

import org.webbitserver.gwt.shared.Server;

/**
 * 
 * @todo setProtocol, setPath, setHost methods
 * 
 */
public interface ServerBuilder<S extends Server<S, ?>> {
	/**
	 * Sets the full url, including protocol, host, port, and path
	 * @param url
	 * @return
	 */
	ServerBuilder<S> setUrl(String url);


	/**
	 * Creates a new instance of the specified server type, starts, and returns it. May
	 * be called more than once to create additional connections, such as after the first
	 * is closed.
	 * 
	 * @return
	 */
	S start();
}
