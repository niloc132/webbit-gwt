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
package com.colinalworth.gwt.websockets.apt.model;

import com.colinalworth.gwt.websockets.shared.Client;
import com.colinalworth.gwt.websockets.shared.Endpoint;
import com.colinalworth.gwt.websockets.shared.Endpoint.BaseClass;
import com.colinalworth.gwt.websockets.shared.Endpoint.RemoteEndpointSupplier;
import com.colinalworth.gwt.websockets.shared.Server;
import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.vertispan.gwtapt.JTypeUtils;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Wraps a given interface to be used as an endpoint, and describes how to build it. Combined with its counterpart
 * (if any), serializers can be created. This is Comparable, ordered on the classname of the endpoint type.
 */
public class EndpointModel implements Comparable<EndpointModel> {

	public static EndpointModel from(Element annotatedEndpoint, ProcessingEnvironment env) {
		TypeMirror clientType = env.getElementUtils().getTypeElement(Client.class.getName()).asType();
		DeclaredType match = JTypeUtils.asParameterizationOf(env.getTypeUtils(), annotatedEndpoint.asType(), clientType);
		if (match != null) {
			return new EndpointModel(annotatedEndpoint, match);
		}

		TypeMirror serverType = env.getElementUtils().getTypeElement(Server.class.getName()).asType();
		match = JTypeUtils.asParameterizationOf(env.getTypeUtils(), annotatedEndpoint.asType(), serverType);
		if (match != null) {
			return new EndpointModel(annotatedEndpoint, match);
		}

		return new EndpointModel(annotatedEndpoint, null);
	}

	// the interface to be implemented
	private final Element endpointElement;

	// the interface which is extended - right now just a few checks to delegate
	// work, but eventually should be more pluggable. this may be null
	private final DeclaredType extraContractType;

	public EndpointModel(Element endpointElement, DeclaredType extraContractType) {
		this.endpointElement = endpointElement;
		this.extraContractType = extraContractType;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		EndpointModel that = (EndpointModel) o;

		return endpointElement.equals(that.endpointElement);
	}

	@Override
	public int hashCode() {
		return endpointElement.hashCode();
	}

	@Override
	public int compareTo(EndpointModel o) {
		return ClassName.get(endpointElement.asType()).toString().compareTo(ClassName.get(o.endpointElement.asType()).toString());
	}

	@Override
	public String toString() {
		return "EndpointModel{" +
				"endpointElement=" + endpointElement +
				", extraContractType=" + extraContractType +
				'}';
	}

	public EndpointModel getMatchingEndpoint(ProcessingEnvironment env) {
		TypeMirror annotationValue = readClassValueFromAnnotation(env, () -> endpointElement.getAnnotation(Endpoint.class).value());
		assert !annotationValue.getKind().isPrimitive();

		if (!TypeName.get(annotationValue).toString().equals(Object.class.getName())) {
			//something was provided, though it might just be the "nothing else to provide" sentinal
			return EndpointModel.from(MoreTypes.asElement(annotationValue), env);
		}

		// otherwise, we rely on there being an extraContactType, and check its generics
		//TODO build out a model for this
		if (extraContractType == null) {
			//no other options, fail
			throw new IllegalStateException("No corresponding endpoint found for " + this + ", did you forget to extend another interface, or add a value to @Endpoint?");
		}

		EndpointModel matching = from(env.getTypeUtils().asElement(extraContractType.getTypeArguments().get(1)), env);

//		// confirm that it matches us back again, else something is misconfigured
//		if (!this.equals(matching.getMatchingEndpoint(env))) {
//			throw new IllegalStateException("Expected " + matching + " to have " + this + " as its remote endpoint, but found " + matching.getMatchingEndpoint(env) + " instead.");
//		}
		return matching;
	}

	public List<EndpointMethod> getEndpointMethods(ProcessingEnvironment env) {
		// walk up the type hierarchy, collect all methods
		return JTypeUtils.getFlattenedSupertypeHierarchy(env.getTypeUtils(), endpointElement.asType())
				.stream()
				.map(MoreTypes::asElement)
				.map(Element::getEnclosedElements)
				.map(ElementFilter::methodsIn)
				.flatMap(List::stream)

				// skip all static and default methods
				.filter(method -> !method.getModifiers().contains(Modifier.STATIC))
				.filter(method -> !method.getModifiers().contains(Modifier.DEFAULT))

				// skip any method declared on our extra contract type
				.filter(method -> extraContractType == null || !ClassName.get(method.getEnclosingElement().asType()).equals(ClassName.get(extraContractType.asElement().asType())))
				// skip anything on Object
				.filter(method -> !Object.class.getName().equals(ClassName.get(method.getEnclosingElement().asType()).toString()))

				// if necessary, express the method as it was declared on the original interface
				// model it, and return a list
				.map(methodElt -> {
					ExecutableType method = (ExecutableType) env.getTypeUtils().asMemberOf((DeclaredType) endpointElement.asType(), methodElt);
					return new EndpointMethod(method, methodElt);
				})

				.peek(method -> method.validate(env))
				.collect(Collectors.toList());
	}

	public String getGeneratedTypeName() {
		return endpointElement.getSimpleName().toString() + "_Impl";
	}

	public String getPackage(ProcessingEnvironment env) {
		return env.getElementUtils().getPackageOf(endpointElement).getQualifiedName().toString();
	}

	public TypeName getInterface() {
		return ClassName.get(endpointElement.asType());
	}

	public TypeName getSpecifiedSuperclass(ProcessingEnvironment env) {
		if (extraContractType != null) {
			BaseClass annotation = extraContractType.asElement().getAnnotation(BaseClass.class);
			DeclaredType supertype = (DeclaredType) readClassValueFromAnnotation(env, annotation::value);
			if (extraContractType.getTypeArguments().isEmpty()) {
				return ClassName.get(supertype);
			}
			return ParameterizedTypeName.get(
					ClassName.get((TypeElement) supertype.asElement()),
					JTypeUtils.findParameterizationOf(env.getTypeUtils(), endpointElement.asType(), extraContractType)
							.stream()
							.map(ClassName::get)
							.toArray(TypeName[]::new)
			);
		}
		return null;
	}

	private static TypeMirror readClassValueFromAnnotation(ProcessingEnvironment env, Supplier<Class> code) {
		try {
			return env.getElementUtils().getTypeElement(code.get().getName()).asType();
		} catch (MirroredTypeException mte) {
			return mte.getTypeMirror();
		}
	}

	public String getRemoteEndpointGetterMethodName() {
		final Element typeToCheck;
		if (extraContractType != null) {
			typeToCheck = extraContractType.asElement();
		} else {
			typeToCheck = endpointElement;
		}
		return ElementFilter.methodsIn(typeToCheck.getEnclosedElements())
				.stream()
				.filter(m -> m.getAnnotation(RemoteEndpointSupplier.class) != null)
				.findFirst()
				.map(m -> m.getSimpleName().toString())
				.orElse(null);
	}
}
