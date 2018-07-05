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

import com.colinalworth.gwt.websockets.client.WebSocket;
import com.colinalworth.gwt.websockets.shared.Endpoint;
import com.google.auto.common.BasicAnnotationProcessor.ProcessingStep;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;

import javax.lang.model.element.Element;
import java.lang.annotation.Annotation;
import java.util.Set;

public class EndpointProcessingStep implements ProcessingStep {
	@Override
	public Set<? extends Class<? extends Annotation>> annotations() {
		return Sets.newHashSet(
				Endpoint.class
//				WebSocket.class
		);
	}

	@Override
	public Set<? extends Element> process(SetMultimap<Class<? extends Annotation>, Element> setMultimap) {
		return null;
	}
}
