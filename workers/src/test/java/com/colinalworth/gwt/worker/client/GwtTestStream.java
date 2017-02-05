package com.colinalworth.gwt.worker.client;

import com.colinalworth.gwt.worker.client.impl.StreamReader;
import com.colinalworth.gwt.worker.client.impl.StreamWriter;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.junit.DoNotRunWith;
import com.google.gwt.junit.Platform;
import com.google.gwt.junit.client.GWTTestCase;
import com.google.gwt.typedarrays.shared.ArrayBuffer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.impl.Serializer;
import com.google.gwt.user.client.rpc.impl.SerializerBase;
import playn.html.TypedArrayHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@DoNotRunWith(Platform.Devel)
public class GwtTestStream extends GWTTestCase {
	@Override
	public String getModuleName() {
		return "com.colinalworth.gwt.worker.RpcToWorkers";
	}

	protected final Serializer s = new SerializerBase(null, null, null, null){};

	protected StreamWriter getStreamWriter() {
		StreamWriter writer = new StreamWriter(s);
		writer.setFlags(0);
		return writer;
	}

	protected StreamReader getStreamReader(StreamWriter writer) {
		JsArrayMixed workerData = writer.getWorkerData();

		ByteBuffer byteBuffer = TypedArrayHelper.wrap((ArrayBuffer) workerData.getObject(0));
		byteBuffer.order(ByteOrder.nativeOrder());
		return new StreamReader(s, byteBuffer, workerData.getObject(1).<JsArrayString>cast());
	}

	protected StreamReader getCompressedStreamReader(StreamWriter writer) {
		return new StreamReader(s, writer.getSocketData().buffer());
	}

	public void testInt() throws Exception {
		StreamWriter writer = getStreamWriter();
		writer.writeInt(4);
		writer.writeInt(1);
		writer.writeInt(Integer.MAX_VALUE);
		writer.writeInt(Integer.MIN_VALUE);
		writer.writeInt(1);

		StreamReader reader = getStreamReader(writer);

		assertEquals(4, reader.readInt());
		assertEquals(1, reader.readInt());
		assertEquals(Integer.MAX_VALUE, reader.readInt());
		assertEquals(Integer.MIN_VALUE, reader.readInt());
		assertEquals(1, reader.readInt());
	}

	public void testLong() throws Exception {
		StreamWriter writer = getStreamWriter();
		writer.writeLong(4);
		writer.writeLong(1);
		writer.writeLong(Integer.MAX_VALUE);
		writer.writeLong(Integer.MIN_VALUE);
		writer.writeLong(1);
		writer.writeLong(Long.MAX_VALUE);
		writer.writeLong(Long.MIN_VALUE);

		StreamReader reader = getStreamReader(writer);

		assertEquals(4L, reader.readLong());
		assertEquals(1L, reader.readLong());
		assertEquals((long)Integer.MAX_VALUE, reader.readLong());
		assertEquals((long)Integer.MIN_VALUE, reader.readLong());
		assertEquals(1L, reader.readLong());
		assertEquals(Long.MAX_VALUE, reader.readLong());
		assertEquals(Long.MIN_VALUE, reader.readLong());
	}

	public void testString() throws Exception {
		StreamWriter writer = getStreamWriter();

		writer.writeString("foo");
		writer.writeString("foo1");
		writer.writeString("foo");
		writer.writeString("bar");

		StreamReader reader = getStreamReader(writer);

		assertEquals("foo", reader.readString());
		assertEquals("foo1", reader.readString());
		assertEquals("foo", reader.readString());
		assertEquals("bar", reader.readString());
	}


	public void testFloat() throws Exception {
		StreamWriter writer = getStreamWriter();

		writer.writeFloat(1.23f);
		writer.writeFloat(3);
		writer.writeFloat(4);
		writer.writeFloat(1.23f);
		writer.writeFloat(Float.MAX_VALUE);
		writer.writeFloat(Float.MIN_VALUE);
		writer.writeFloat(Float.MIN_NORMAL);
		writer.writeFloat(Float.NaN);
		writer.writeFloat(Float.NEGATIVE_INFINITY);
		writer.writeFloat(Float.POSITIVE_INFINITY);

		StreamReader reader = getStreamReader(writer);

		assertEquals(1.23f, reader.readFloat());
		assertEquals(3.0f, reader.readFloat());
		assertEquals(4.0f, reader.readFloat());
		assertEquals(1.23f, reader.readFloat());
		assertEquals(Float.MAX_VALUE, reader.readFloat());
		assertEquals(Float.MIN_VALUE, reader.readFloat());
		assertEquals(Float.MIN_NORMAL, reader.readFloat());
		assertTrue(Float.isNaN(reader.readFloat()));
		assertEquals(Float.NEGATIVE_INFINITY, reader.readFloat());
		assertEquals(Float.POSITIVE_INFINITY, reader.readFloat());

	}

	public void testDouble() throws Exception {
		StreamWriter writer = getStreamWriter();

		writer.writeDouble(1.23);
		writer.writeDouble(3);
		writer.writeDouble(4);
		writer.writeDouble(1.23);
		writer.writeDouble(Double.MAX_VALUE);
		writer.writeDouble(Double.MIN_VALUE);
		writer.writeDouble(Double.MIN_NORMAL);
		writer.writeDouble(Double.NaN);
		writer.writeDouble(Double.NEGATIVE_INFINITY);
		writer.writeDouble(Double.POSITIVE_INFINITY);

		StreamReader reader = getStreamReader(writer);

		assertEquals(1.23, reader.readDouble());
		assertEquals(3.0, reader.readDouble());
		assertEquals(4.0, reader.readDouble());
		assertEquals(1.23, reader.readDouble());
		assertEquals(Double.MAX_VALUE, reader.readDouble());
		assertEquals(Double.MIN_VALUE, reader.readDouble());
		assertEquals(Double.MIN_NORMAL, reader.readDouble());
		assertTrue(Double.isNaN(reader.readDouble()));
		assertEquals(Double.NEGATIVE_INFINITY, reader.readDouble());
		assertEquals(Double.POSITIVE_INFINITY, reader.readDouble());

	}


	public void testIntCompressed() throws Exception {
		delayTestFinish(10000);
		ScriptInjector.fromUrl("pako.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
			@Override
			public void onFailure(Exception e) {
				fail();
			}

			@Override
			public void onSuccess(Void aVoid) {
				StreamWriter writer = getStreamWriter();
				writer.writeLong(4);
				writer.writeLong(1);
				writer.writeLong(Integer.MAX_VALUE);
				writer.writeLong(Integer.MIN_VALUE);
				writer.writeLong(1);
				writer.writeLong(Long.MAX_VALUE);
				writer.writeLong(Long.MIN_VALUE);

				StreamReader reader = getCompressedStreamReader(writer);

				try {
					assertEquals(4L, reader.readLong());
					assertEquals(1L, reader.readLong());
					assertEquals((long)Integer.MAX_VALUE, reader.readLong());
					assertEquals((long)Integer.MIN_VALUE, reader.readLong());
					assertEquals(1L, reader.readLong());
					assertEquals(Long.MAX_VALUE, reader.readLong());
					assertEquals(Long.MIN_VALUE, reader.readLong());
				} catch (SerializationException e) {
					throw new RuntimeException(e);
				}
				finishTest();
			}
		}).inject();
	}

	public void testLongCompressed() throws Exception {
		delayTestFinish(10000);
		ScriptInjector.fromUrl("pako.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
			@Override
			public void onFailure(Exception e) {
				fail();
			}

			@Override
			public void onSuccess(Void aVoid) {
				StreamWriter writer = getStreamWriter();
				writer.writeInt(4);
				writer.writeInt(1);
				writer.writeInt(Integer.MAX_VALUE);
				writer.writeInt(Integer.MIN_VALUE);
				writer.writeInt(1);

				StreamReader reader = getCompressedStreamReader(writer);

				try {
					assertEquals(4, reader.readInt());
					assertEquals(1, reader.readInt());
					assertEquals(Integer.MAX_VALUE, reader.readInt());
					assertEquals(Integer.MIN_VALUE, reader.readInt());
					assertEquals(1, reader.readInt());
				} catch (SerializationException e) {
					throw new RuntimeException(e);
				}
				finishTest();
			}
		}).inject();
	}

	public void testStringCompressed() throws Exception {
		delayTestFinish(10000);
		ScriptInjector.fromUrl("pako.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
		/**/	@Override
			public void onFailure(Exception e) {
				fail();
			}

			@Override
			public void onSuccess(Void aVoid) {
				StreamWriter writer = getStreamWriter();

				writer.writeString("foo");
				writer.writeString("foo1");
				writer.writeString("foo");
				writer.writeString("bar");

				StreamReader reader = getCompressedStreamReader(writer);

				try {
					assertEquals("foo", reader.readString());
					assertEquals("foo1", reader.readString());
					assertEquals("foo", reader.readString());
					assertEquals("bar", reader.readString());
				} catch (SerializationException e) {
					throw new RuntimeException(e);
				}
				finishTest();
			}


		}).inject();
	}

	public void testDoubleCompressed() throws Exception {
		delayTestFinish(10000);
		ScriptInjector.fromUrl("pako.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
			@Override
			public void onFailure(Exception e) {
				fail();
			}

			@Override
			public void onSuccess(Void aVoid) {
				StreamWriter writer = getStreamWriter();

				writer.writeDouble(1.23);
				writer.writeDouble(3);
				writer.writeDouble(4);
				writer.writeDouble(1.23);
				writer.writeDouble(Double.MAX_VALUE);
				writer.writeDouble(Double.MIN_VALUE);
				writer.writeDouble(Double.MIN_NORMAL);
				writer.writeDouble(Double.NaN);
				writer.writeDouble(Double.NEGATIVE_INFINITY);
				writer.writeDouble(Double.POSITIVE_INFINITY);

				StreamReader reader = getCompressedStreamReader(writer);

				try {
					assertEquals(1.23, reader.readDouble());
					assertEquals(3.0, reader.readDouble());
					assertEquals(4.0, reader.readDouble());
					assertEquals(1.23, reader.readDouble());
					assertEquals(Double.MAX_VALUE, reader.readDouble());
					assertEquals(Double.MIN_VALUE, reader.readDouble());
					assertEquals(Double.MIN_NORMAL, reader.readDouble());
					assertTrue(Double.isNaN(reader.readDouble()));
					assertEquals(Double.NEGATIVE_INFINITY, reader.readDouble());
					assertEquals(Double.POSITIVE_INFINITY, reader.readDouble());
				} catch (SerializationException e) {
					throw new RuntimeException(e);
				}
				finishTest();
			}
		}).inject();
	}
	public void testFloatCompressed() throws Exception {
		delayTestFinish(10000);
		ScriptInjector.fromUrl("pako.js").setWindow(ScriptInjector.TOP_WINDOW).setCallback(new Callback<Void, Exception>() {
			@Override
			public void onFailure(Exception e) {
				fail();
			}

			@Override
			public void onSuccess(Void aVoid) {
				StreamWriter writer = getStreamWriter();

				writer.writeFloat(1.23f);
				writer.writeFloat(3);
				writer.writeFloat(4);
				writer.writeFloat(1.23f);
				writer.writeFloat(Float.MAX_VALUE);
				writer.writeFloat(Float.MIN_VALUE);
				writer.writeFloat(Float.MIN_NORMAL);
				writer.writeFloat(Float.NaN);
				writer.writeFloat(Float.NEGATIVE_INFINITY);
				writer.writeFloat(Float.POSITIVE_INFINITY);

				StreamReader reader = getCompressedStreamReader(writer);

				try {
					assertEquals(1.23f, reader.readFloat());
					assertEquals(3.0f, reader.readFloat());
					assertEquals(4.0f, reader.readFloat());
					assertEquals(1.23f, reader.readFloat());
					assertEquals(Float.MAX_VALUE, reader.readFloat());
					assertEquals(Float.MIN_VALUE, reader.readFloat());
					assertEquals(Float.MIN_NORMAL, reader.readFloat());
					assertTrue(Float.isNaN(reader.readFloat()));
					assertEquals(Float.NEGATIVE_INFINITY, reader.readFloat());
					assertEquals(Float.POSITIVE_INFINITY, reader.readFloat());
				} catch (SerializationException e) {
					throw new RuntimeException(e);
				}
				finishTest();
			}
		}).inject();
	}


}
