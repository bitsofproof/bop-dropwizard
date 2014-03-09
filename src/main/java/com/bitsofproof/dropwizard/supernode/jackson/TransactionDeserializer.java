/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.common.WireFormat;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.fasterxml.jackson.databind.introspect.Annotated;

import java.io.IOException;
import java.util.Objects;

public class TransactionDeserializer extends FromStringDeserializer<Transaction> implements ContextualDeserializer
{
	protected TransactionDeserializer ()
	{
		super (Transaction.class);
	}

	@Override
	protected Transaction _deserialize (String value, DeserializationContext ctxt) throws IOException
	{
		throw new IllegalStateException ("This method should be implemented by a subclass");
	}

	@Override
	public JsonDeserializer<?> createContextual (DeserializationContext ctxt, BeanProperty property) throws JsonMappingException
	{
		if ( property != null )
		{
			JsonFormat.Value format = ctxt.getAnnotationIntrospector ().findFormat ((Annotated) property.getMember ());
			if ( format != null && Objects.equals (TransactionSerializer.BASE64_FORMAT, format.getPattern ()) )
			{
				return new Base64TransactionDeserializer ();
			}
		}

		return new HexTransactionDeserializer ();
	}

	private static class HexTransactionDeserializer extends TransactionDeserializer
	{
		@Override
		protected Transaction _deserialize (String value, DeserializationContext ctxt) throws IOException
		{
			return Transaction.fromWireDump (value);
		}
	}

	private static class Base64TransactionDeserializer extends TransactionDeserializer
	{
		@Override
		protected Transaction _deserialize (String value, DeserializationContext ctxt) throws IOException
		{
			byte[] bytes = ctxt.getConfig ().getBase64Variant ().decode (value);
			return Transaction.fromWire (new WireFormat.Reader (bytes));
		}
	}
}
