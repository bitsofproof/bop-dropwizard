package com.bitsofproof.dropwizard.supernode.jackson;

import com.bitsofproof.supernode.wallet.Address;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SupernodeModule extends SimpleModule {

	public SupernodeModule ()
	{
		addDeserializer (Address.class, new AddressDeserializer());
		addSerializer (Address.class, new AddressSerializer());
	}
}
