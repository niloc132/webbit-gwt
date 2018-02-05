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

public class ServerCallbackInvocation_CustomFieldSerializer extends CustomFieldSerializer<ServerCallbackInvocation> {
	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, ServerCallbackInvocation instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	public static void deserialize(SerializationStreamReader streamReader, ServerCallbackInvocation instance) throws SerializationException {
		instance.setCallbackId(streamReader.readInt());
		instance.setSuccess(streamReader.readBoolean());
		instance.setResponse(streamReader.readObject());
	}

	@Override
	public void serializeInstance(SerializationStreamWriter streamWriter, ServerCallbackInvocation instance) throws SerializationException {
		serialize(streamWriter, instance);
	}

	public static void serialize(SerializationStreamWriter streamWriter, ServerCallbackInvocation instance) throws SerializationException {
		streamWriter.writeInt(instance.getCallbackId());
		streamWriter.writeBoolean(instance.isSuccess());
		streamWriter.writeObject(instance.getResponse());
	}
}
