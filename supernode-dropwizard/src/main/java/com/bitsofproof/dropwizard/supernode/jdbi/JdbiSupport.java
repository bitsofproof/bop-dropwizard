package com.bitsofproof.dropwizard.supernode.jdbi;

import org.skife.jdbi.v2.DBI;

public class JdbiSupport
{

	public static void supportSupernode(DBI dbi)
	{
		dbi.registerArgumentFactory (new AddressArgumentFactory());
	}
}
