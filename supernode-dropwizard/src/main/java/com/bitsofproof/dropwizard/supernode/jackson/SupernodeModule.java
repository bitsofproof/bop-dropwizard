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

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.Transaction;
import com.bitsofproof.supernode.common.ExtendedKey;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SupernodeModule extends SimpleModule
{

	public interface IgnoreExtendedKeyIsReadOnly
	{
		@JsonIgnore
		boolean isReadOnly();
	}

	public SupernodeModule ()
	{
		addDeserializer (ExtendedKey.class, new ExtendedKeyDeserializer ());
		addDeserializer (Address.class, new AddressDeserializer ());
		addDeserializer (Transaction.class, new TransactionDeserializer());

		addSerializer(ExtendedKey.class, new ExtendedKeySerializer ());
		addSerializer (Address.class, new AddressSerializer ());
		addSerializer (Transaction.class, new TransactionSerializer ());

		setMixInAnnotation (ExtendedKey.class, IgnoreExtendedKeyIsReadOnly.class);
	}
}
