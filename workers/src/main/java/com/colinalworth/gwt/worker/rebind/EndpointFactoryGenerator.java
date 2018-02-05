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
package com.colinalworth.gwt.worker.rebind;

import com.colinalworth.gwt.worker.client.WorkerFactory;
import com.colinalworth.gwt.worker.client.impl.AbstractWorkerFactoryImpl;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Name;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

import java.io.PrintWriter;

/**
 * Generator for ServerBuilder instances.
 *
 */
public class EndpointFactoryGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();

		if (toGenerate == null) {
			logger.log(Type.ERROR, "Error generating " + typeName + ", either not an interface, or cannot be reached from client code.");
			throw new UnableToCompleteException();
		}
		JClassType workerFactoryType = oracle.findType(WorkerFactory.class.getName());
		JClassType remoteEndpointType = ModelUtils.findParameterizationOf(workerFactoryType, toGenerate)[0];
		JClassType localEndpointType = ModelUtils.findParameterizationOf(workerFactoryType, toGenerate)[1];

		// Build an impl so we can call it ourselves
		RemoteEndpointCreator creator = new RemoteEndpointCreator(remoteEndpointType);
		creator.create(logger, context);

		String packageName = toGenerate.getPackage().getName();
		String simpleName = toGenerate.getName().replace('.', '_') + "_Impl";

		PrintWriter pw = context.tryCreate(logger, packageName, simpleName);
		if (pw == null) {
			return packageName + "." + simpleName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleName);
		factory.setSuperclass(Name.getSourceNameForClass(AbstractWorkerFactoryImpl.class) + "<" + remoteEndpointType.getQualifiedSourceName() + "," + localEndpointType.getQualifiedSourceName() + ">");
		factory.addImplementedInterface(typeName);

		SourceWriter sw = factory.createSourceWriter(context, pw);


//		RemoteServiceRelativePath path = remoteEndpointType.getAnnotation(RemoteServiceRelativePath.class);
//		if (path != null) {
//			sw.println("public %1$s() {", simpleName);
//			sw.indentln("setPath(\"%1$s\");", path.value());
//			sw.println("}");
//		}

		sw.println();
		// start method
//		sw.println("public %1$s start() {", remoteEndpointType.getQualifiedSourceName());
//		sw.indent();
//		sw.println("String url = getUrl();");
//		sw.println("if (url == null) {");
//		sw.indentln("return new %1$s(getErrorHandler());", creator.getQualifiedSourceName());
//		sw.println("} else {");
//		sw.indentln("return new %1$s(getErrorHandler(), url);", creator.getQualifiedSourceName());
//		sw.println("}");
//
//		sw.outdent();
//		sw.println("}");

		// create method
		sw.println("protected %1$s create(%2$s worker) {", remoteEndpointType.getQualifiedSourceName(), MessagePort.class.getName());
		sw.indentln("return new %1$s(worker);", creator.getQualifiedSourceName());
		sw.println("}");

		sw.commit(logger);

		return factory.getCreatedClassName();

	}

}
