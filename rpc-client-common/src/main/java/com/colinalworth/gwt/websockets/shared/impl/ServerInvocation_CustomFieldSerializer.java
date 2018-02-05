/*
 * #%L
 * rpc-client-common
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
package com.colinalworth.gwt.websockets.shared.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class ServerInvocation_CustomFieldSerializer extends CustomFieldSerializer<ServerInvocation> {
	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, ServerInvocation instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	public static void deserialize(SerializationStreamReader streamReader, ServerInvocation instance) throws SerializationException {
		instance.setMethod(streamReader.readString());
		instance.setCallbackId(streamReader.readInt());

		int length = streamReader.readInt();
		Object[] params = new Object[length];
		for (int i = 0; i < length; i++) {
			params[i] = streamReader.readObject();
		}
		instance.setParameters(params);
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, ServerInvocation instance) throws SerializationException {
		serialize(streamWriter, instance);
	}

	public static void serialize(SerializationStreamWriter streamWriter,ServerInvocation instance) throws SerializationException {
		streamWriter.writeString(instance.getMethod());
		streamWriter.writeInt(instance.getCallbackId());

		streamWriter.writeInt(instance.getParameters().length);
		Object[] parameters = instance.getParameters();
		for (int i = 0; i < parameters.length; i++) {
			Object param = parameters[i];
			streamWriter.writeObject(param);
		}
	}
}
