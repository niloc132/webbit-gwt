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
package com.colinalworth.gwt.worker.rebind;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Server;
import com.colinalworth.gwt.worker.client.Endpoint;
import com.colinalworth.gwt.worker.client.impl.AbstractWorkerEndpointImpl;
import com.colinalworth.gwt.worker.client.impl.RemoteCallbackInvocation;
import com.colinalworth.gwt.worker.client.impl.RemoteInvocation;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.JParameter;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Name;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.google.gwt.user.rebind.rpc.SerializableTypeOracleBuilder;
import com.google.gwt.user.rebind.rpc.TypeSerializerCreator;

import java.io.PrintWriter;

/**
 * Creates a new instance of the given Server type
 *
 */
public class RemoteEndpointCreator {
	/*
	 * The type we are generating an implementation for
	 */
	private final JClassType remoteEndpointType;

	public RemoteEndpointCreator(JClassType remoteEndpointType) {
		this.remoteEndpointType = remoteEndpointType;
	}

	public void create(TreeLogger logger, GeneratorContext context) throws UnableToCompleteException {
		String remoteEndpointName = this.remoteEndpointType.getQualifiedSourceName();

		String packageName = getPackageName();
		String simpleName = getSimpleName();

		TypeOracle oracle = context.getTypeOracle();

		PrintWriter pw = context.tryCreate(logger, packageName, simpleName);
		if (pw == null) {
			return;
		}
		JClassType endpointType = oracle.findType(Name.getSourceNameForClass(Endpoint.class));
		JClassType localEndpointType = ModelUtils.findParameterizationOf(endpointType, this.remoteEndpointType)[1];

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleName);
		factory.setSuperclass(Name.getSourceNameForClass(AbstractWorkerEndpointImpl.class) + "<" + remoteEndpointName + "," + localEndpointType.getQualifiedSourceName() + ">");
		factory.addImplementedInterface(remoteEndpointName);

		SourceWriter sw = factory.createSourceWriter(context, pw);

//		//TODO move this check before the printwriter creation can fail, and allow the warn to be optional
//		sw.println("public %1$s(%2$s errorHandler) {", simpleName, Name.getSourceNameForClass(ServerBuilder.ConnectionErrorHandler.class));
//		RemoteServiceRelativePath path = this.remoteEndpointType.getAnnotation(RemoteServiceRelativePath.class);
//		if (path == null) {
////			logger.log(Type.WARN, "@RemoteServiceRelativePath required on " + remoteEndpointName + " to make a connection to the server without a ServerBuilder");
////			throw new UnableToCompleteException();
//			sw.indentln("super(null);");
//			sw.indentln("throw new RuntimeException(\"@RemoteServiceRelativePath annotation required on %1$s to make a connection without a path defined in ServerBuilder\");");
//		} else {
//			sw.indentln("super(errorHandler, " +
//							"com.google.gwt.user.client.Window.Location.getProtocol().toLowerCase().startsWith(\"https\") ? \"wss://\": \"ws://\", " +
//							"com.google.gwt.user.client.Window.Location.getHost(), \"%1$s\");", path.value());
//		}
//		sw.println("}");
//
//		sw.println("public %1$s(%2$s errorHandler, String url) {", simpleName, Name.getSourceNameForClass(ServerBuilder.ConnectionErrorHandler.class));
//		sw.indentln("super(errorHandler, url);");
//		sw.println("}");

		sw.println("public %1$s(%2$s worker) {", simpleName ,MessagePort.class.getName());
		sw.indentln("super(worker);");
		sw.println("}");

		//Find all types that may go over the wire
		// Collect the types the server will send to the client using the Client interface
		SerializableTypeOracleBuilder serverSerializerBuilder = new SerializableTypeOracleBuilder(logger, context);
		appendMethodParameters(logger, localEndpointType, Endpoint.class, serverSerializerBuilder);
		appendCallbackParameters(logger, this.remoteEndpointType, Server.class, serverSerializerBuilder);
		// Also add the wrapper object ClientInvocation
		serverSerializerBuilder.addRootType(logger, oracle.findType(RemoteInvocation.class.getName()));
		serverSerializerBuilder.addRootType(logger, oracle.findType(RemoteCallbackInvocation.class.getName()));

		// Collect the types the client will send to the server using the Server interface
		SerializableTypeOracleBuilder clientSerializerBuilder = new SerializableTypeOracleBuilder(logger, context);
		appendMethodParameters(logger, this.remoteEndpointType, Server.class, clientSerializerBuilder);
		appendCallbackParameters(logger, localEndpointType, Endpoint.class, clientSerializerBuilder);
		// Also add the ServerInvocation wrapper
		clientSerializerBuilder.addRootType(logger, oracle.findType(RemoteInvocation.class.getName()));
		clientSerializerBuilder.addRootType(logger, oracle.findType(RemoteCallbackInvocation.class.getName()));

		String tsName = simpleName + "_TypeSerializer";
		TypeSerializerCreator serializerCreator = new TypeSerializerCreator(logger, clientSerializerBuilder.build(logger), serverSerializerBuilder.build(logger), context, packageName + "." + tsName, tsName);
		serializerCreator.realize(logger);

		// Make the newly created Serializer available at runtime
		sw.println("protected %1$s __getSerializer() {", Serializer.class.getName());
		sw.indentln("return %2$s.<%1$s>create(%1$s.class);", tsName, GWT.class.getName());
		sw.println("}");

		// Build methods that call from the client to the server
		for (JMethod m : this.remoteEndpointType.getInheritableMethods()) {
			if (isRemoteMethod(m, Endpoint.class)) {
				printServerMethodBody(logger, context, sw, m);
			}
		}


		// Read incoming calls and dispatch them to the correct client method
		sw.println("protected void __invoke(String method, Object[] params) {");
		for (JMethod m : localEndpointType.getInheritableMethods()) {
			if (isRemoteMethod(m, Endpoint.class)) {
				JParameter[] params = m.getParameters();
				sw.println("if (method.equals(\"%1$s\") && params.length == %2$d) {", m.getName(), params.length);
				sw.indent();
				sw.println("getRemote().%1$s(", m.getName());
				sw.indent();
				for (int i = 0; i < params.length; i++) {
					if (i != 0) {
						sw.print(",");
					}
					sw.println("(%1$s)params[%2$d]", params[i].getType().getQualifiedSourceName(), i);
				}
				sw.outdent();
				sw.println(");");
				sw.outdent();
				sw.println("}");
			}
		}
		sw.println("}");


		sw.println("protected void __onError(Exception error) {");
		//TODO
		sw.println("}");

		sw.commit(logger);
	}


	public String getPackageName() {
		return remoteEndpointType.getPackage().getName();
	}
	public String getSimpleName() {
		return remoteEndpointType.getName().replace('.', '_') + "_ProxyImpl";
	}

	public String getQualifiedSourceName() {
		return getPackageName() + "." + getSimpleName();
	}



	/**
	 * Helper method to build up the list of types that can go over the wire
	 * @param logger
	 * @param serviceInterface
	 * @param serviceSuperClass
	 * @param serializerBuilder
	 */
	private void appendMethodParameters(TreeLogger logger, JClassType serviceInterface, Class<?> serviceSuperClass, SerializableTypeOracleBuilder serializerBuilder) {
		TreeLogger l = logger.branch(Type.DEBUG, "Adding params types to " + serviceInterface.getName());
		for (JMethod m : serviceInterface.getMethods()) {
			if (isRemoteMethod(m, serviceSuperClass)) {
				JParameter[] parameters = m.getParameters();
				for (int i = 0; i < parameters.length; i++) {
					JParameter param = parameters[i];
					if (i + 1 != m.getParameters().length || param.getType().isInterface() == null || !param.getType().isInterface().getQualifiedSourceName().equals(Callback.class.getName())) {
					  serializerBuilder.addRootType(l, param.getType());
					}
				}
			}
		}
	}

	private void appendCallbackParameters(TreeLogger logger, JClassType serviceInterface, Class<?> serviceSuperClass, SerializableTypeOracleBuilder serializerBuilder) {
		TreeLogger l = logger.branch(Type.DEBUG, "Adding callback types to" + serviceInterface.getName());
		for (JMethod m : serviceInterface.getMethods()) {
			if (isRemoteMethod(m, serviceSuperClass)) {
				JParameter[] parameters = m.getParameters();
				for (int i = 0; i < parameters.length; i++) {
					JParameter param = parameters[i];
					if (i + 1 == m.getParameters().length && param.getType().isInterface() != null && param.getType().isInterface().getQualifiedSourceName().equals(Callback.class.getName())) {
						//read generics from Callback<T, F> and add as root types
						JClassType t = param.getType().isParameterized().getTypeArgs()[0];
						JClassType f = param.getType().isParameterized().getTypeArgs()[1];
						serializerBuilder.addRootType(l, t);
						serializerBuilder.addRootType(l, f);
					}
				}
			}
		}
	}

	/**
	 * Writes out the method to use to invoke a server call. Mostly derived from RPC's way of building proxy methods
	 *
	 * @param logger
	 * @param context
	 * @param sw
	 * @param m
	 */
	private void printServerMethodBody(TreeLogger logger, GeneratorContext context, SourceWriter sw, JMethod m) {
		sw.println("%1$s {", m.getReadableDeclaration(false, true, true, true, true));
		sw.indent();

		sw.print("__sendMessage(\"%1$s\"", m.getName());
		StringBuilder sb = new StringBuilder();
		String callback = "null";
		JParameter[] parameters = m.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			JParameter param = parameters[i];
			if (i + 1 == parameters.length && param.getType().isInterface() != null && param.getType().isInterface().getQualifiedSourceName().equals(Callback.class.getName())) {
				callback = param.getName();
			} else {
				sb.append(",\n").append(param.getName());
			}
		}
		sw.print(",");
		sw.println(callback);
		sw.print(sb.toString());
		sw.println(");");

		sw.outdent();
		sw.println("}");
	}

	/**
	 * Checks to see if the given method can be called over the wire.
	 *
	 *
	 * @param m method to check
	 * @param superClass either {@link Server} or {@link Client} or {@link Endpoint}, indicating which direction the call will be made
	 * @return
	 */
	private boolean isRemoteMethod(JMethod m, Class<?> superClass) {
		assert superClass == Server.class || superClass == Client.class || superClass == Endpoint.class;
		return !m.getEnclosingType().getQualifiedSourceName().equals(superClass.getName());
	}
}
