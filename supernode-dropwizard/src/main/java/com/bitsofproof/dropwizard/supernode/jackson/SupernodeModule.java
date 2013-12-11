package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.api.Transaction;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SupernodeModule extends SimpleModule
{

	public SupernodeModule ()
	{
		addDeserializer (Address.class, new AddressDeserializer ());
		addDeserializer (Transaction.class, new TransactionDeserializer());

		addSerializer (Address.class, new AddressSerializer ());
		addSerializer (Transaction.class, new TransactionSerializer ());
	}
}
