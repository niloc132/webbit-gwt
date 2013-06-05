GWT library to make RPC calls over websockets to a webbit/netty server. As in regular RPC, the client can
call the server at any time, but with this library, the server can also call back the the client.

WebSockets are established from the client to the server, and while open are maintained to allow them
to continue to communicate over the same channel. Either side can send a string to the other at any time.
This project uses this to enable one-way RPC methods to easily be sent.

At present, the default setup for this is to make all methods return `void` - no `AsyncCallbacks` are
specified. This simplifies the number of interfaces required to create and maintain for a project - there
is no need to create both a 'normal' interface and a matching 'async' interface. There are still two
interfaces required though, but they do not need to keep in sync with each other - one represents the
calls the server can make to the client, and the other represents the calls the client can make to the
server. For those calls which require a callback, a matching 'callback method' can be defined in the other
interface.

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
possible implementation, made deliberatly simple for the sake of an example. For a larger solution, some
kind of message queue could be used to send out messages to the proper recipients.

Check out the [webbit-gwt-sample](webbit-gwt-sample/) project for a working, runnable example of the above
code.