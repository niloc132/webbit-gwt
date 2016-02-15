package com.colinalworth.gwt.worker.client.impl;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class RemoteInvocation_CustomFieldSerializer extends CustomFieldSerializer<RemoteInvocation> {
	@Override
	public void deserializeInstance(SerializationStreamReader streamReader, RemoteInvocation instance) throws SerializationException {
		deserialize(streamReader, instance);
	}

	public static void deserialize(SerializationStreamReader streamReader, RemoteInvocation instance) throws SerializationException {
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
	public void serializeInstance(SerializationStreamWriter streamWriter, RemoteInvocation instance) throws SerializationException {
		serialize(streamWriter, instance);
	}

	public static void serialize(SerializationStreamWriter streamWriter, RemoteInvocation instance) throws SerializationException {
		streamWriter.writeString(instance.getMethod());
		streamWriter.writeInt(instance.getCallbackId());
		if (instance.getParameters() == null) {
			streamWriter.writeInt(0);
		} else {
			streamWriter.writeInt(instance.getParameters().length);
			Object[] parameters = instance.getParameters();
			for (int i = 0; i < parameters.length; i++) {
				Object param = parameters[i];
				streamWriter.writeObject(param);
			}
		}
	}
}
