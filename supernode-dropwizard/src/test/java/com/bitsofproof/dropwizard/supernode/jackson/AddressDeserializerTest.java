package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.Address;
import com.bitsofproof.supernode.wallet.AddressConverter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AddressDeserializerTest {

	public static final String TEST_ADDRESS = "13woBeJcGWhnJ2xGETphgBmrrNLJPbYyzX";

	ObjectMapper mapper;

	@Before
	public void setUp () throws Exception
	{
		mapper = new ObjectMapper ();
		mapper.registerModule (new SupernodeModule ());
	}

	private String quoteAddress()
	{
		return String.format("\"%s\"", TEST_ADDRESS);
	}

	@Test
	public void testDeserialize () throws Exception
	{
		mapper.readValue (quoteAddress(), Address.class);
		assertNull(mapper.readValue ("\"\"", Address.class));
	}

	@Test
	public void testSerialize() throws ValidationException, JsonProcessingException
	{
		Address address = AddressConverter.fromSatoshiStyle (TEST_ADDRESS);
		assertEquals (quoteAddress(), mapper.writeValueAsString (address));
	}
}
