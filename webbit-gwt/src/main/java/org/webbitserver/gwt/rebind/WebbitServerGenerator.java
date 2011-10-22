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

import org.webbitserver.gwt.client.impl.ServerImpl;
import org.webbitserver.gwt.shared.Server;

import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.TreeLogger.Type;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JMethod;
import com.google.gwt.core.ext.typeinfo.TypeOracle;
import com.google.gwt.dev.util.Name;
import com.google.gwt.editor.rebind.model.ModelUtils;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;

/**
 * @author colin
 *
 */
public class WebbitServerGenerator extends Generator {

	@Override
	public String generate(TreeLogger logger, GeneratorContext context,
			String typeName) throws UnableToCompleteException {

		TypeOracle oracle = context.getTypeOracle();
		JClassType toGenerate = oracle.findType(typeName).isInterface();

		if (toGenerate == null) {
			logger.log(Type.ERROR, "Error generating " + typeName + ", either not an interface, or cannot be reached from client code.");
			throw new UnableToCompleteException();
		}

		String packageName = toGenerate.getPackage().getName();
		String simpleName = toGenerate.getName().replace('.', '_') + "_ProxyImpl";

		PrintWriter pw = context.tryCreate(logger, packageName, simpleName);
		if (pw == null) {
			return packageName + "." + simpleName;
		}
		JClassType serverType = oracle.findType(Name.getSourceNameForClass(Server.class));
		String clientType = ModelUtils.findParameterizationOf(serverType, toGenerate)[1].getQualifiedSourceName();

		ClassSourceFileComposerFactory factory = new ClassSourceFileComposerFactory(packageName, simpleName);
		factory.setSuperclass(Name.getSourceNameForClass(ServerImpl.class) + "<" + typeName + "," + clientType + ">");
		factory.addImplementedInterface(typeName);

		SourceWriter sw = factory.createSourceWriter(context, pw);

		sw.println("public %1$s() {", simpleName);
		sw.indentln("super(\"/chat\");");//TODO replace with data from annotation or something
		sw.println("}");

		for (JMethod m : toGenerate.getMethods()) {
			if (isRemoteMethod(m)) {
				printMethodBody(logger, context, sw, m);
			}
		}
		sw.println("protected void __onMessage(String message) {");
		sw.println("}");
		sw.println("protected void __onError(Exception message) {");
		sw.println("}");

		sw.commit(logger);

		return factory.getCreatedClassName();
	}

	/**
	 * @param logger
	 * @param context
	 * @param sw
	 * @param m
	 */
	private void printMethodBody(TreeLogger logger, GeneratorContext context,
			SourceWriter sw, JMethod m) {
		sw.println("%1$s {", m.getReadableDeclaration(false, true, true, true, true));
		sw.println();
		sw.println("}");
	}

	/**
	 * @param m
	 * @return
	 */
	private boolean isRemoteMethod(JMethod m) {
		return !m.getEnclosingType().getQualifiedSourceName().equals(Server.class.getName());
	}

}
