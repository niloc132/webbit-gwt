/*
 * #%L
 * workers
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
package com.colinalworth.gwt.worker.client.impl;

import com.colinalworth.gwt.worker.client.pako.Deflate;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.typedarrays.shared.ArrayBufferView;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.AbstractSerializationStreamWriter;
import com.google.gwt.user.client.rpc.impl.Serializer;
import playn.html.HasArrayBufferView;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
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
 * version, flags, payloadLength, payload, stringtableLength, stringtable
 *
 * The last two are optional if there are no strings.
 *
 * Now we just have one array instead of two - all strings are gzipped and then concat'd into
 *
 */
public class StreamWriter extends AbstractSerializationStreamWriter {
	//constants from BigLongLibBase for long->int[] conversion
	protected static final int BITS = 22;
	protected static final int BITS01 = 2 * BITS;
	protected static final int BITS2 = 64 - BITS01;
	protected static final int MASK = (1 << BITS) - 1;
	protected static final int MASK_2 = (1 << BITS2) - 1;

	private ByteBuffer bb;//initial size 1kb
	private IntBuffer payload;

	private final Serializer serializer;


	public StreamWriter(Serializer serializer) {
		this.serializer = serializer;
		bb = ByteBuffer.allocate(8 << 2);
		bb.order(ByteOrder.nativeOrder());
		payload = bb.asIntBuffer();
		payload.position(3);//leave room for flags, version, size
	}



	public JsArrayMixed getWorkerData() {
		//shrink down to actual space used
		//TODO consider replacing with a List<IntBuffer> and make new ones as needed, then make a big one, or send them all
//		payload = payload.compact();
		payload.limit(payload.position());
		payload.position(0);
		payload = payload.slice();

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
//		payload = payload.compact();
		payload.limit(payload.position());
		payload.position(0);
		payload = payload.slice();

		//TODO don't compress if small enough!
		Deflate zip = new Deflate();

		payload.put(0, getFlags());
		payload.put(1, getVersion());
		//mark the size of the payload
		payload.put(2, payload.limit() - 3);

		//shift bb to the same offsets as payload, then slice so we have a typed array with the right bounds
		bb.limit(payload.limit() << 2);
		bb.position(payload.position() << 2);
		bb = bb.slice();//http://thecodelesscode.com/case/209
		payload = null;

		List<String> strings = getStringTable();
		ArrayBufferView typedArray = ((HasArrayBufferView) bb).getTypedArray();
		zip.push(typedArray, strings.isEmpty());

		if (!strings.isEmpty()) {
			IntBuffer length = IntBuffer.allocate(1);
			length.put(0, strings.size());
			ArrayBuffer lengthBuffer = ((HasArrayBufferView) length).getTypedArray().buffer();
			zip.push(lengthBuffer, false);
			for (int i = 0; i < strings.size(); i++) {
				String string = strings.get(i);
				length.put(0, string.length());
				zip.push(lengthBuffer, false);
				zip.push(string, i + 1 == strings.size());
			}
		}

		return zip.getResult();
	}

	private native JavaScriptObject jso(Object jso) /*-{
		return jso;
	}-*/;
	private native JavaScriptObject jso(Object[] jso) /*-{
		return jso.slice();//rebuild the array but without gwt's bookkeeping properties
	}-*/;

	@Override
	public String toString() {
		return "StreamWriter";
	}

	@Override
	public void writeLong(long l) {
		//impl from BigLongLibBase
		int[] a = new int[3];
		a[0] = (int) (l & MASK);
		a[1] = (int) ((l >> BITS) & MASK);
		a[2] = (int) ((l >> BITS01) & MASK_2);
		writeInt(a[0]);
		writeInt(a[1]);
		writeInt(a[2]);
		assert false : "longs not yet implemented";
	}

	public void writeBoolean(boolean fieldValue) {
		writeInt(fieldValue ? 1 : 0);//wasteful, but no need to try to pack this way
	}

	@Override
	public void writeByte(byte fieldValue) {
		writeInt(fieldValue);//wasteful, but no need to try to pack this way
	}

	@Override
	public void writeChar(char ch) {
		writeInt(ch);
	}

	@Override
	public void writeFloat(float fieldValue) {
		maybeGrow();
		bb.asFloatBuffer().put(payload.position(), fieldValue);
		payload.position(payload.position() + 1);
	}

	@Override
	public void writeDouble(double fieldValue) {
		writeLong(Double.doubleToLongBits(fieldValue));
	}

	@Override
	public void writeInt(int fieldValue) {
		maybeGrow();
		payload.put(fieldValue);
	}

	private void maybeGrow() {
		if (!payload.hasRemaining()) {
			ByteBuffer old = bb;
			bb = ByteBuffer.allocate(old.capacity() * 2);
			bb.order(ByteOrder.nativeOrder());
			IntBuffer oldPayload = payload;
			payload = bb.asIntBuffer();
			payload.put((IntBuffer)oldPayload.flip());
		}
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
