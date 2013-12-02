package com.bitsofproof.dropwizard.supernode.jackson;

import java.io.IOException;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public class AddressDeserializer extends StdScalarDeserializer<Address>
{
	protected AddressDeserializer ()
	{
		super (Address.class);
	}

	@Override
	public Address deserialize (JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
	{
		JsonToken t = jp.getCurrentToken ();
		if ( t == JsonToken.VALUE_STRING )
		{
			try
			{
				String satoshiStyle = jp.getText ().trim ();
				if ( satoshiStyle.length () == 0 )
				{
					return null;
				}

				return Address.fromSatoshiStyle (satoshiStyle);
			}
			catch ( ValidationException e )
			{
				JsonMappingException.from (jp, "Error deserializing bitcoin address", e);
			}
		}

		throw ctxt.mappingException (getValueClass ());
	}
}
