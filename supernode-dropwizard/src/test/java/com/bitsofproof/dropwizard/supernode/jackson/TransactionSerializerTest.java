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
