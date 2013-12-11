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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TransactionSerializerTest
{

	private ObjectMapper mapper;

	private Transaction tx;

	private String expectedSerialization;

	@Before
	public void setUp()
	{
		mapper = new ObjectMapper ();
		mapper.registerModule (new SupernodeModule ());

		tx = new Transaction ();
		expectedSerialization = String.format ("\"%s\"", tx.toWireDump ());
	}

	@Test
	public void testDeserialize() throws IOException
	{
		assertNull (mapper.readValue ("\"\"", Transaction.class));
		assertNotNull(mapper.readValue(expectedSerialization, Transaction.class));
	}

	@Test
	public void testSerialize() throws JsonProcessingException
	{
		assertEquals(expectedSerialization, mapper.writeValueAsString (tx));
	}
}
