package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.pako.Inflate;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamReader;
import com.google.gwt.user.client.rpc.impl.Serializer;
import playn.html.TypedArrayHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class StreamReader extends AbstractSerializationStreamReader {
	private final Serializer serializer;//b
	private final IntBuffer payload;
	private final JsArrayString strings;

	public StreamReader(Serializer serializer, IntBuffer payload, JsArrayString strings) {
		this.serializer = serializer;
		this.payload = payload;
		this.strings = strings;
		int version = payload.get();
		int flags = payload.get();
		int length = payload.get();
		assert length == payload.remaining();
		setVersion(version);
		setFlags(flags);
	}

	public StreamReader(Serializer serializer, ArrayBuffer data) {
		this.serializer = serializer;

		//TODO if StreamWriter might not compress, check that we've got something that can be compressed...
		//unzip data before continuing
		Inflate unzip = Inflate.create();
		unzip.push(data, true);
		data = unzip.getResult().buffer();

		ByteBuffer byteBuffer = TypedArrayHelper.wrap(data);
		byteBuffer.order(ByteOrder.nativeOrder());
		IntBuffer ints = byteBuffer.asIntBuffer();
		int version = ints.get();
		int flags = ints.get();
		int length = ints.get();
		setVersion(version);
		setFlags(flags);
		payload = ints.slice();
		payload.limit(length);

		strings = JsArrayString.createArray().cast();
		if (ints.limit() > 3 + length) {//if there is at least one entry after payload
			int stringsCount = ints.get(3 + length);
			assert stringsCount >= 0;//shouldn't have written count for zero strings
			byteBuffer.position((4 + length) << 2);

			for (int i = 0; i < stringsCount; i++) {
				int stringLength = byteBuffer.getInt();
				byte[] bytes = new byte[stringLength];
				byteBuffer.get(bytes);
				strings.push(new String(bytes));
			}
		}
	}

	@Override
	public void prepareToRead(String encoded) throws SerializationException {
		assert false : "Do not call this, serialized strings are not supported";
	}

	@Override
	protected Object deserialize(String s) throws SerializationException {
		int id = reserveDecodedObjectIndex();
		Object instance = serializer.instantiate(this, s);
		rememberDecodedObject(id, instance);
		serializer.deserialize(this, instance, s);
		return instance;
	}

	@Override
	protected String getString(int i) {
		return i > 0 ? strings.get(i - 1) : null;
	}

	@Override
	public boolean readBoolean() throws SerializationException {
		return payload.get() == 1;//or zero
	}

	@Override
	public byte readByte() throws SerializationException {
		return (byte) payload.get();
	}

	@Override
	public char readChar() throws SerializationException {
		return (char) payload.get();
	}

	@Override
	public double readDouble() throws SerializationException {
		assert false : "doubles not yet implemented";
		return 0;
	}

	@Override
	public float readFloat() throws SerializationException {
		assert false : "floats not yet implemented";
		return 0;
	}

	@Override
	public int readInt() throws SerializationException {
		return payload.get();
	}

	@Override
	public long readLong() throws SerializationException {
		assert false : "longs not yet implemented";
		return 0;
	}

	@Override
	public short readShort() throws SerializationException {
		return (short) payload.get();
	}

	@Override
	public String readString() throws SerializationException {
		return getString(readInt());
	}
}
