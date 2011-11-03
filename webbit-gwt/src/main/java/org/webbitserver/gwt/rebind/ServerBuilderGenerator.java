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
package org.webbitserver.gwt.rebind;

import java.io.PrintWriter;

import org.webbitserver.gwt.client.ServerBuilder;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * Generator for ServerBuilder instances.
 *
 */
public class ServerBuilderGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {
		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();

		if (toGenerate == null) {
			logger.log(Type.ERROR, "Error generating " + typeName + ", either not an interface, or cannot be reached from client code.");
			throw new UnableToCompleteException();
		}
		JClassType serverBuilderType = oracle.findType(ServerBuilder.class.getName());
		JClassType serverImplType = ModelUtils.findParameterizationOf(serverBuilderType, toGenerate)[0];

		// Build an impl so we can call it ourselves
		ServerCreator creator = new ServerCreator(serverImplType);
		creator.create(logger, context);

		String packageName = toGenerate.getPackage().getName();
		String simpleName = toGenerate.getName().replace('.', '_') + "_Impl";

		PrintWriter pw = context.tryCreate(logger, packageName, simpleName);
		if (pw == null) {
			return packageName + "." + simpleName;
		}

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleName);
		factory.addImplementedInterface(typeName);

		SourceWriter sw = factory.createSourceWriter(context, pw);

		//private var for url
		//TODO will need more vars for complete url building
		sw.println("private String url = null;");


		// setUrl method
		sw.println("public %1$s setUrl(String url) {", typeName);
		sw.indentln("this.url = url;");
		sw.indentln("return this;");
		sw.println("}");

		sw.println();
		// start method
		sw.println("public %1$s start() {", serverImplType.getQualifiedSourceName());
		sw.indent();

		sw.println("if (url == null) {");
		sw.indentln("return new %1$s();", creator.getQualifiedSourceName());
		sw.println("} else {");
		sw.indentln("return new %1$s(url);", creator.getQualifiedSourceName());
		sw.println("}");

		sw.outdent();
		sw.println("}");

		sw.commit(logger);

		return factory.getCreatedClassName();

	}

}
