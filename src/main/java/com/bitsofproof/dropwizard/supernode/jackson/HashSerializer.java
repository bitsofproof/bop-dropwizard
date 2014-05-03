package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.Hash;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class HashSerializer extends StdSerializer<Hash>
{
	public HashSerializer ()
	{
		super(Hash.class);
	}

	@Override
	public void serialize (Hash value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
	{
		jgen.writeString (value.toString ());
	}
}
