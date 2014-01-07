/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.common.WireFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionSerializerTest
{

	private ObjectMapper mapper;

	private Transaction tx;

	private String expectedSerialization;

	private HexTx hexTx;

	private Base64Tx base64Tx;

	private String expectedHexSerialization;

	private String expectedBase64Serialization;

	@Before
	public void setUp()
	{
		mapper = new ObjectMapper ();
		mapper.registerModule (new SupernodeModule ());

		tx = new Transaction ();
		hexTx = new HexTx(tx);
		base64Tx = new Base64Tx (tx);

		expectedSerialization = String.format ("\"%s\"", tx.toWireDump ());
		expectedHexSerialization = String.format("{\"tx\":%s}", expectedSerialization);

		WireFormat.Writer writer = new WireFormat.Writer ();
		tx.toWire (writer);
		expectedBase64Serialization = String.format("{\"tx\":\"%s\"}", mapper.getSerializationConfig ().getBase64Variant ().encode (writer.toByteArray ()));
	}

	@Test
	public void testDeserialize() throws IOException
	{
		assertNull (mapper.readValue ("\"\"", Transaction.class));
		assertNotNull(mapper.readValue(expectedSerialization, Transaction.class));
		assertNotNull(mapper.readValue(expectedBase64Serialization, Base64Tx.class));
		assertNotNull(mapper.readValue(expectedHexSerialization, HexTx.class));
	}

	@Test
	public void testSerialize() throws JsonProcessingException
	{
		assertEquals(expectedSerialization, mapper.writeValueAsString (tx));
	}

	@Test
	public void testFormats() throws JsonProcessingException
	{
		assertEquals(expectedHexSerialization, mapper.writeValueAsString (hexTx));


		mapper.getSerializationConfig ().getBase64Variant ();

		assertEquals (expectedBase64Serialization, mapper.writeValueAsString (base64Tx));
	}

	public static class HexTx
	{
		@JsonProperty
		@JsonFormat(pattern = "hex")
		public Transaction tx;

		public HexTx ()
		{
		}

		public HexTx (Transaction tx)
		{
			this.tx = tx;
		}
	}

	public static class Base64Tx
	{
		@JsonProperty
		@JsonFormat(pattern = "base64")
		public Transaction tx;

		public Base64Tx ()
		{
		}

		public Base64Tx (Transaction tx)
		{
			this.tx = tx;
		}
	}

}
