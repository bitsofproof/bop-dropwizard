package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.ExtendedKey;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class ExtendedKeyDeserializer extends StdScalarDeserializer<ExtendedKey>
{

	protected ExtendedKeyDeserializer ()
	{
		super (ExtendedKey.class);
	}

	@Override
	public ExtendedKey deserialize (JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		JsonToken t = jp.getCurrentToken ();
		if ( t == JsonToken.VALUE_STRING )
		{
			try
			{
				String keyString = jp.getText ().trim ();
				if ( keyString.length () == 0 )
				{
					return null;
				}

				return ExtendedKey.parse (keyString);
			}
			catch (ValidationException e)
			{
				JsonMappingException.from (jp, "Error deserializing extended key", e);
			}
		}

		throw ctxt.mappingException (getValueClass ());
	}
}
