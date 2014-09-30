(Name in flux, probably will be renamed to gwt-websockets)

GWT library to make RPC calls over websockets. As in regular GWT-RPC, the client can call the server at
any time, but with this library, the server can also call back the the client.

WebSockets are established from the client to the server, and while open are maintained to allow them
to continue to communicate over the same channel. Either side can send a string to the other at any time.
This project uses this to enable one-way RPC methods to easily be sent.

The default setup for this is to only use a pair of interfaces, one for the client, and one for the server.
Since the client API needs to know about the server and vice versa, generics are used so that either side
of the API can see to the other.

Methods can optionally take a `Callback<T,F>` object as the final parameter, but instead of the server
implementing the same method synchronously, both client and server code are written to assume async behavior.
Note however that callbacks are often not required - messages can be passed without *expecting* a reply back.
Note also that callbacks are one-time use, and cannot be invoked multiple times - use a different method
on the opposite interface to achieve that effect.

Two server implementations are supported - Webbit (a Netty-based websocket server), and
[JSR-356](https://www.jcp.org/en/jsr/detail?id=356) (the spec for javax.websocket, implemented by
[Glassfish](https://tyrus.java.net/),
[Jetty](http://www.eclipse.org/jetty/documentation/current/jetty-javaee.html#jetty-javaee-7), and
[Tomcat](tomcat.apache.org/tomcat-7.0-doc/web-socket-howto.html)). The library uses version 1.0 of this
API, as Jetty (and perhaps others) do not yet support 1.1.


## Example

### Client-Server Contract

These interfaces refer to each other in their generics. Here is a simple client interface for a chat app:

    /**
     * Simple example of methods implemented by a GWT client that can be called from the server
     *
     */
    public interface ChatClient extends Client<ChatClient, ChatServer> {
    	/**
    	 * Tells the client that a user posted a message to the chat room
    	 * @param username the user who sent the message
    	 * @param message the message the user sent
    	 */
    	void say(String username, String message);

    	/**
    	 * Indicates that a new user has entered the chat room
    	 * @param username the user who logged in
    	 */
    	void join(String username);

    	/**
    	 * Indicates that a user has left the chat room
    	 * @param username the user who left
    	 */
    	void part(String username);
    }

The client code must implement this interface, and will be called when the server invokes any of those
methods. Once implemented, the client may do anything with these details - fire events to the rest of the
app, call other methods, directly interact with the UI, etc.

    /**
     * Simple example of methods a server can have that can be invoked by a client.
     *
     */
    @RemoteServiceRelativePath("/chat")
    public interface ChatServer extends Server<ChatServer, ChatClient> {
    	/**
    	 * Brings the user into the chat room, with the given username
    	 * @param username the name to use
    	 */
    	void login(String username);

    	/**
    	 * Sends the given message to the chatroom
    	 * @param message the message to say to the room
    	 */
    	void say(String message);
    }

In this matching server interface, we can see the messages that any client can send to the server after
connecting. The server in turn must implement this, and may call back to the client using any of the
methods defined in the first interface.

### Client Wiring
The client first builds an implementation of the client interface - this will be its way of recieving all
'callbacks' from the server. Then, we declare an interface to connect to the server:

    interface ChatServerBuilder extends ServerBuilder<ChatServer> {}

This interface can then be implemented with `GWT.create`, given a url and client instance, and the
connection established:

    		// Create an instance of the build
    		final ChatServerBuilder builder = GWT.create(ChatServerBuilder.class);
    		// Set the url to connect to - may or may not be the same as the original server
    		builder.setUrl("ws://" + Window.Location.getHost() + "/chat");

    		// Get an instance of the client impl
    		ChatClient impl = ...;

    		// Start a connection to the server, and attach the client impl
    		final ChatServer server = builder.start();
    		server.setClient(impl);

Once the `onOpen()` method gets called on the client object, the connection is established, and the client
may invoke any server method until `onClose()` is called. To restart the connection (or start another sim
simultaneous connect), call `start()` again and then talk to the newly returned server object.

The `AbstractClientImpl` class can serve as a handy base class, providing default implementations of the
`onOpen()` and `onClose()` methods that fire events.

### Server Wiring

With either API there is an `AbstractServerImpl` class. This provides the working details of the `Server`
interface as well as the specifics how to interact with either Webbit or JSR-356.

From within either client or server implementation, you can always get a reference to the other side - the
server can call `getClient()`, and the client already has an instance (see below). Our ChatServerImpl
probably needs to track all connected clients, so we'll start off with a field to hold them:

    public class ChatServerImpl extends AbstractServerImpl<ChatServer, ChatClient> implements ChatServer {
    	private final Map<ChatClient, String> loggedIn =
    	            Collections.synchronizedMap(new HashMap<ChatClient, String>());

Next, we'll want to implement each method - for example, when any user says something, we'll send it to all
other connected users:

    	@Override
    	public void say(String message) {
    		ChatClient c = getClient();
    		String userName = loggedIn.get(c);

    		for (ChatClient connected : loggedIn.keySet()) {
    			connected.say(userName, message);
    		}
    	}

Note that this is not the only way to keep several clients in communication with each other, just one
possible implementation, made deliberately simple for the sake of an example. For a larger solution, some
kind of message queue could be used to send out messages to the proper recipients.

### JSR-356
For the `javax.websocket` api, we have two basic options, as documented at http://docs.oracle.com/javaee/7/tutorial/doc/websocket.htm.
The simplest way to do this is usually the [annotated approach](http://docs.oracle.com/javaee/7/tutorial/doc/websocket004.htm#BABFEBGA),
by which the endpoint class must be decorated with `@ServerEndpoint()` to indicate the url it should be
used to respond to. The other annotations are already present in the `RpcEndpoint` class (the superclass
of `AbstractServerImpl`).

    @ServerEndpoint("/chat")
    public class ChatServerImpl extends AbstractServerImpl<ChatServer, ChatClient> implements ChatServer {
    	//...

Check out the [javaee-websocket-gwt-rpc-sample](javaee-websocket-gwt-rpc-sample/) project for a working,
runnable example of the above code.

### Webbit
In the server must create an instance implementing its own interface, though it must also establish the
rest of the server. In this example, we have a `main()` method that starts the app and host both an http
server as well as a websocket server:

    	public static void main(String[] args) throws IOException {
    		//start a webserver on port 9876...
    		WebServer webServer = WebServers.createWebServer(9876)
    		//...with some local resources (see webbit docs for details)...
    		.add(new EmbeddedResourceHandler("static"))
    		//...and a url that can be connected to via websockets
    		.add("/chat", new GwtWebService<ChatServer,ChatClient>(new ChatServerImpl(), ChatClient.class))
    		.start();

    		System.out.println("Chat room running on: " + webServer.getUri());
    	}

The `GwtWebServer` instance is the websocket wiring - the `ChatServerImpl` is an implementation of the
`ChatServer` interface. As above, there is a `AbstractServerImpl` class to provide some of the basics in
a server implementation, looking after the currently active connection in the thread, and providing
default no-op implementations to make getting started easier.

Check out the [webbit-gwt-rpc-sample](webbit-gwt-rpc-sample/) project for a working, runnable example of
the above code.
