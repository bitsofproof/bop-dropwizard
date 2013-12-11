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

import java.io.IOException;
import java.lang.reflect.Type;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

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
			jgen.writeString (Address.toSatoshiStyle (value));
		}
		catch ( ValidationException e )
		{
			throw new JsonMappingException ("Error serializing address", e);
		}
	}

	@Override
	public JsonNode getSchema (SerializerProvider provider, Type typeHint) throws JsonMappingException
	{
		return createSchemaNode ("string");
	}
}
