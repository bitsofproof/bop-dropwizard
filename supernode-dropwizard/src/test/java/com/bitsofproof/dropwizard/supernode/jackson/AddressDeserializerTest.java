package com.bitsofproof.dropwizard.supernode.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AddressDeserializerTest
{

	public static final String TEST_ADDRESS = "13woBeJcGWhnJ2xGETphgBmrrNLJPbYyzX";

	ObjectMapper mapper;

	@Before
	public void setUp () throws Exception
	{
		mapper = new ObjectMapper ();
		mapper.registerModule (new SupernodeModule ());
	}

	private String quoteAddress ()
	{
		return String.format ("\"%s\"", TEST_ADDRESS);
	}

	@Test
	public void testDeserialize () throws Exception
	{
		mapper.readValue (quoteAddress (), Address.class);
		assertNull (mapper.readValue ("\"\"", Address.class));
	}

	@Test
	public void testSerialize () throws ValidationException, JsonProcessingException
	{
		Address address = Address.fromSatoshiStyle (TEST_ADDRESS);
		assertEquals (quoteAddress (), mapper.writeValueAsString (address));
	}
}
