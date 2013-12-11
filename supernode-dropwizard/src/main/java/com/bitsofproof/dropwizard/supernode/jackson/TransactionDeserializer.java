package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.api.Transaction;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;

import java.io.IOException;

public class TransactionDeserializer extends FromStringDeserializer<Transaction>
{
	protected TransactionDeserializer ()
	{
		super (Transaction.class);
	}

	@Override
	protected Transaction _deserialize (String value, DeserializationContext ctxt) throws IOException
	{
		return Transaction.fromWireDump (value);
	}
}
