package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.pako.Deflate;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;
import playn.html.HasArrayBufferView;

import java.nio.IntBuffer;
import java.util.List;

/**
 *
 *
 * Header (int version, int flags, int payloadLength (empty))
 * Payload (header then int[])
 * StringTable (String[])
 *
 * Basic worker format:
 *
 * [payload, stringtable]
 *
 *
 * For workers, we can actually push through the Header+Payload as a single arraybuffer and skip copying it, but the
 * StringTable has to be copied.
 *
 *
 *
 * Basic socket format:
 *
 * Now we just have one array instead of two - all strings are gzipped and then concat'd into
 *
 */
public class StreamWriter extends AbstractSerializationStreamWriter {
	private IntBuffer payload = IntBuffer.allocate(1024);//initial size 1kb

	private final Serializer serializer;


	public StreamWriter(Serializer serializer) {
		this.serializer = serializer;
		payload.position(3);//leave room for flags, version, size
	}



	public JsArrayMixed getWorkerData() {
		//shrink down to actual space used
		//TODO consider replacing with a List<IntBuffer> and make new ones as needed, then make a big one, or send them all
//		payload = payload.compact();
		payload.limit(payload.position());

		payload.put(0, getFlags());
		payload.put(1, getVersion());
		//mark the size of the payload
		payload.put(2, payload.limit() - 3);

		JsArrayMixed value = JsArrayMixed.createArray().cast();
		value.push(jso(((HasArrayBufferView) payload).getTypedArray().buffer()));
		value.push(jso(getStringTable().toArray(new String[0])));

		payload = null;//finalized, can't go back
		return value;
	}

	public ArrayBufferView getSocketData() {
		payload = payload.compact();

		Deflate zip = Deflate.create();

		payload.put(0, getFlags());
		payload.put(1, getVersion());
		//mark the size of the payload
		payload.put(2, payload.limit() - 3);

		zip.push(((HasArrayBufferView) payload).getTypedArray(), false);
		payload = null;

		List<String> strings = getStringTable();
		IntBuffer length = IntBuffer.allocate(1);
		for (int i = 0; i < strings.size(); i++) {
			String string = strings.get(i);
			length.put(0, string.length());
			zip.push(((HasArrayBufferView) length).getTypedArray(), false);
			zip.push(string, i + 1 == strings.size());
		}

		return zip.getResult();
	}

	private native JavaScriptObject jso(Object jso) /*-{
		return jso;
	}-*/;

	@Override
	public String toString() {
		return "StreamWriter";
	}

	@Override
	public void writeLong(long l) {
		//TODO
		assert false : "longs not yet implemented";
	}

	public void writeBoolean(boolean fieldValue) {
		writeInt(fieldValue ? 1 : 0);
	}

	@Override
	public void writeByte(byte fieldValue) {
		writeInt(fieldValue);
	}

	@Override
	public void writeChar(char ch) {
		writeInt(ch);
	}

	@Override
	public void writeFloat(float fieldValue) {
		super.writeFloat(fieldValue);
	}

	@Override
	public void writeDouble(double fieldValue) {
		//TODO
		assert false : "doubles not yet implemented";
	}

	@Override
	public void writeInt(int fieldValue) {
		if (!payload.hasRemaining()) {
			IntBuffer old = payload;
			payload = IntBuffer.allocate(old.capacity() * 2);
			payload.put(old);
		}
		payload.put(fieldValue);
	}

	@Override
	public void writeShort(short value) {
		writeInt(value);
	}

	@Override
	protected void append(String s) {
		//strangely enough, we do nothing, and wait until we actually are asked to write the whole thing out
	}

	@Override
	protected String getObjectTypeSignature(Object o) throws SerializationException {
		Class clazz = o.getClass();
		if(o instanceof Enum) {
			Enum e = (Enum)o;
			clazz = e.getDeclaringClass();
		}

		return this.serializer.getSerializationSignature(clazz);
	}

	@Override
	protected void serialize(Object o, String s) throws SerializationException {
		this.serializer.serialize(this, o, s);
	}
}
