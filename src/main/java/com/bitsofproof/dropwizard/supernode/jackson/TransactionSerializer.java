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
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Objects;

public class TransactionSerializer extends StdSerializer<Transaction> implements ContextualSerializer
{
	public static final String HEX_FORMAT = "hex";
	public static final String BASE64_FORMAT = "base64";

	public TransactionSerializer ()
	{
		super (Transaction.class);
	}

	private static class HexTransactionSerializer extends TransactionSerializer
	{
		@Override
		public void serialize (Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException
		{
			if (value != null)
				jgen.writeString (value.toWireDump ());
		}
	}

	private static class Base64TransactionSerializer extends TransactionSerializer
	{
		@Override
		public void serialize (Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException
		{
			if (value != null)
			{
				WireFormat.Writer writer = new WireFormat.Writer ();
				value.toWire (writer);
				jgen.writeBinary (writer.toByteArray ());
			}
		}
	}


	@Override
	public void serialize (Transaction value, JsonGenerator jgen, SerializerProvider provider) throws IOException
	{
		throw new IllegalStateException ("This method should be implemented by a subclass");
	}

	@Override
	public JsonNode getSchema (SerializerProvider provider, Type typeHint) throws JsonMappingException
	{
		return createSchemaNode("string");
	}

	@Override
	public JsonSerializer<?> createContextual (SerializerProvider prov, BeanProperty property) throws JsonMappingException
	{
		if (property != null)
		{
			JsonFormat.Value format = prov.getAnnotationIntrospector().findFormat((Annotated)property.getMember());
			if ( format != null && Objects.equals (BASE64_FORMAT, format.getPattern ()))
			{
				return new Base64TransactionSerializer ();
			}
		}

		return new HexTransactionSerializer ();
	}
}
