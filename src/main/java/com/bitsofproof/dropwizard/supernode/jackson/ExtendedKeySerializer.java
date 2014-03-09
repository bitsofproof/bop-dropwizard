package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.ExtendedKey;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ExtendedKeySerializer extends StdSerializer<ExtendedKey>
{
	public ExtendedKeySerializer ()
	{
		super (ExtendedKey.class);
	}

	@Override
	public void serialize (ExtendedKey value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException
	{
		jgen.writeString(value.serialize (true));
	}
}
