///**
// *  Copyright 2011 Colin Alworth
// *
// *  Licensed under the Apache License, Version 2.0 (the "License");
// *  you may not use this file except in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing, software
// *  distributed under the License is distributed on an "AS IS" BASIS,
// *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  See the License for the specific language governing permissions and
// *  limitations under the License.
// *
// */
//package com.colinalworth.gwt.worker.rebind;
//
//import com.google.gwt.core.ext.Generator;
//import com.google.gwt.core.ext.GeneratorContext;
//import com.google.gwt.core.ext.TreeLogger;
//import com.google.gwt.core.ext.TreeLogger.Type;
//import com.google.gwt.core.ext.UnableToCompleteException;
//import com.google.gwt.core.ext.typeinfo.JClassType;
//import com.google.gwt.core.ext.typeinfo.TypeOracle;
//
///**
// * Generator for the {@link com.colinalworth.gwt.websockets.shared.Server} interface. Builds client-side
// * objects that can be used to contact and run methods on the server, and can allow client methods
// * to be invoked from that server.
// *
// */
//public class WebbitServerGenerator extends Generator {
//
//	@Override
//	public String generate(TreeLogger logger, GeneratorContext context,
//			String typeName) throws UnableToCompleteException {
//
//		TypeOracle oracle = context.getTypeOracle();
//		JClassType toGenerate = oracle.findType(typeName).isInterface();
//
//		if (toGenerate == null) {
//			logger.log(Type.ERROR, "Error generating " + typeName + ", either not an interface, or cannot be reached from client code.");
//			throw new UnableToCompleteException();
//		}
//
//		RemoteEndpointCreator creator = new RemoteEndpointCreator(toGenerate);
//
//		creator.create(logger, context);
//
//		return creator.getQualifiedSourceName();
//	}
//}
