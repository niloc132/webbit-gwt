/*
 * #%L
 * gwt-websockets-processor
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
package com.colinalworth.gwt.websockets.apt;

import com.colinalworth.gwt.websockets.apt.model.EndpointModel;
import com.colinalworth.gwt.websockets.apt.model.EndpointPair;
import com.colinalworth.gwt.websockets.shared.Endpoint;
import com.colinalworth.gwt.websockets.shared.impl.AbstractEndpointImpl;
import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;
import com.squareup.javapoet.TypeVariableName;
import com.vertispan.serial.Processor;
import com.vertispan.serial.SerializationStreamReader;
import com.vertispan.serial.SerializationStreamWriter;
import com.vertispan.serial.SerializationWiring;
import com.vertispan.serial.TypeSerializer;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class EndpointProcessingStep implements ProcessingStep {
	private final ProcessingEnvironment processingEnv;

	public EndpointProcessingStep(ProcessingEnvironment processingEnv) {
		this.processingEnv = processingEnv;
	}

	@Override
	public Set<? extends Class<? extends Annotation>> annotations() {
		return Collections.singleton(Endpoint.class);
	}

	@Override
	public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> elementsByAnnotation) {

		Set<Element> endpoints = elementsByAnnotation.get(Endpoint.class);

		endpoints.stream()
				.map(endpoint -> EndpointPair.fromOne(endpoint, processingEnv))
				.distinct()
				.forEach(pair -> {
					implement(pair.getLeft(), pair.getRight());
					implement(pair.getRight(), pair.getLeft());
				});

		return Sets.newHashSet();
	}

	private void implement(EndpointModel model, EndpointModel remoteModel) {
		// set up basics, declare superclass (and extra contract wiring?)
		String packageName = model.getPackage(processingEnv);
		Builder builder = TypeSpec.classBuilder(model.getGeneratedTypeName())
				.superclass(ClassName.get(AbstractEndpointImpl.class))
				.addSuperinterface(model.getInterface())
				.addAnnotation(AnnotationSpec.builder(Generated.class).addMember("value", "\"$L\"", EndpointProcessor.class.getCanonicalName()).build())
		        .addModifiers(Modifier.PUBLIC);
		// create the serializer type
		TypeSpec serializer = declareSerializer(model, remoteModel);
		builder.addType(serializer);

		// create a field for the serializer
		ClassName serializerType = ClassName.get(packageName, model.getGeneratedTypeName(), serializer.name);
		builder.addField(
				serializerType,
				"s",
				Modifier.PRIVATE,
				Modifier.FINAL
		);

		// build constructors
		TypeVariableName sTypeVar = TypeVariableName.get("S", ClassName.get(SerializationStreamWriter.class));
		ParameterizedTypeName writerFactoryType = ParameterizedTypeName.get(
				ClassName.get(Function.class),
				ClassName.get(TypeSerializer.class),
				sTypeVar
		);
		ParameterizedTypeName sendType = ParameterizedTypeName.get(
				ClassName.get(Consumer.class),
				sTypeVar
		);
		ParameterizedTypeName onMessageType = ParameterizedTypeName.get(
				ClassName.get(BiConsumer.class),
				ParameterizedTypeName.get(
						Consumer.class,
						SerializationStreamReader.class
				),
				ClassName.get(TypeSerializer.class)
		);
		builder.addMethod(MethodSpec.constructorBuilder()
				.addTypeVariable(sTypeVar)
				.addParameter(writerFactoryType, "writerFactory")
				.addParameter(sendType, "send")
				.addParameter(onMessageType, "onMessage")
				.addStatement("this(writerFactory, send, new $T_Impl(), onMessage)", serializerType)
				.addModifiers(Modifier.PUBLIC)
				.build());
		builder.addMethod(MethodSpec.constructorBuilder()
				.addTypeVariable(sTypeVar)
				.addParameter(writerFactoryType, "writerFactory")
				.addParameter(sendType, "send")
				.addParameter(onMessageType, "onMessage")
				.addParameter(serializerType, "serializers")
				.addStatement("super(writerFactory, send, serializers.createSerializer(), onMessage)")
				.addStatement("s = serializers")
				.addModifiers(Modifier.PRIVATE)
				.build());

		// create each declared method

		// build __invoke from our matching remote type

		// any extra contract methods


		try {
			JavaFile.builder(packageName, builder.build()).build().writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private TypeSpec declareSerializer(EndpointModel model, EndpointModel remoteModel) {
		Builder builder = TypeSpec.interfaceBuilder(model.getGeneratedTypeName() + "Serializer")
				.addAnnotation(SerializationWiring.class)
				.addModifiers(Modifier.PUBLIC);
		return builder.build();
	}
}
