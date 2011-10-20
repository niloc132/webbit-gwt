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
package samples.easychatroom.server;

import java.io.IOException;

import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.gwt.server.GwtWebService;
import org.webbitserver.handler.EmbeddedResourceHandler;

import samples.easychatroom.shared.ChatClient;

/**
 * @author colin
 *
 */
public class ChatServer {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		WebServer webServer = WebServers.createWebServer(9876)
		.add(new EmbeddedResourceHandler("static"))
		.add("/chat", new GwtWebService<samples.easychatroom.shared.ChatServer,ChatClient>(new ChatServerImpl(), ChatClient.class))
		.start();

		System.out.println("Chat room running on: " + webServer.getUri());
	}

}
