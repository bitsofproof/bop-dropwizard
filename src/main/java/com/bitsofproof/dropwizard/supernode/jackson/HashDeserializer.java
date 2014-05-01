package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.common.Hash;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

import java.io.IOException;

public class HashDeserializer extends StdScalarDeserializer<Hash>
{
	protected HashDeserializer ()
	{
		super (Hash.class);
	}

	@Override
	public Hash deserialize (JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		JsonToken t = jp.getCurrentToken ();
		if (t == JsonToken.VALUE_STRING)
		{
			String hashString = jp.getText ().trim();
			if (hashString.length () == 0)
			{
				return null;
			}

			return new Hash(hashString);
		}

		throw ctxt.mappingException (handledType ());
	}
}
