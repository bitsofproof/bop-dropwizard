package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.Address;
import com.bitsofproof.supernode.wallet.AddressConverter;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class AddressSerializer extends StdSerializer<Address>
{

	public AddressSerializer ()
	{
		super (Address.class);
	}

	@Override
	public void serialize (Address value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
	{
		try
		{
			jgen.writeString (AddressConverter.toSatoshiStyle (value));
		}
		catch (ValidationException e)
		{
			throw new JsonMappingException("Error serializing address", e);
		}
	}

	@Override
	public JsonNode getSchema (SerializerProvider provider, Type typeHint) throws JsonMappingException
	{
		return createSchemaNode ("string");
	}
}
