package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.api.Transaction;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Type;

public class TransactionSerializer extends StdSerializer<Transaction>
{
	public TransactionSerializer ()
	{
		super (Transaction.class);
	}

	@Override
	public void serialize (Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
	{
		if (value != null)
			jgen.writeString (value.toWireDump ());
	}

	@Override
	public JsonNode getSchema (SerializerProvider provider, Type typeHint) throws JsonMappingException
	{
		return createSchemaNode("string");
	}
}
