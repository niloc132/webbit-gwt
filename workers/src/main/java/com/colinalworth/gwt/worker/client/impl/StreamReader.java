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
	private final ByteBuffer bb;
	private final IntBuffer payload;
	private final JsArrayString strings;

	public StreamReader(Serializer serializer, ByteBuffer bb, JsArrayString strings) {
		this.serializer = serializer;
		this.bb = bb;
		this.payload = bb.asIntBuffer();
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
		Inflate unzip = new Inflate();
		unzip.push(data, true);
		data = unzip.getResult().buffer();

		bb = TypedArrayHelper.wrap(data);
		bb.order(ByteOrder.nativeOrder());
		IntBuffer ints = bb.asIntBuffer();
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
			bb.position((4 + length) << 2);

			for (int i = 0; i < stringsCount; i++) {
				int stringLength = bb.getInt();
				byte[] bytes = new byte[stringLength];
				bb.get(bytes);
				strings.push(new String(bytes));
			}
		}
		bb.position(3 << 2);
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
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public float readFloat() throws SerializationException {

		int position = payload.position();
		payload.position(position + 1);
		return bb.asFloatBuffer().get(position);
	}

	@Override
	public int readInt() throws SerializationException {
		return payload.get();
	}

	@Override
	public long readLong() throws SerializationException {
		int[] a = new int[3];
		a[0] = readInt();
		a[1] = readInt();
		a[2] = readInt();
		assert a[0] == (a[0] & StreamWriter.MASK);
		assert a[1] == (a[1] & StreamWriter.MASK);
		assert a[2] == (a[2] & StreamWriter.MASK_2);
		return (long) a[0] + ((long) a[1] << (long) StreamWriter.BITS) + ((long) a[2] << (long) StreamWriter.BITS01);
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
