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
