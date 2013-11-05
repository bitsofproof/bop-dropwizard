package com.bitsofproof.dropwizard.supernode.jdbi;

import com.bitsofproof.supernode.wallet.Address;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class AddressArgumentFactory implements ArgumentFactory<Address>
{
	@Override
	public boolean accepts (Class<?> expectedType, Object value, StatementContext ctx)
	{
		return value instanceof Address;
	}

	@Override
	public Argument build (Class<?> expectedType, Address value, StatementContext ctx)
	{
		return new AddressArgument(value);
	}
}
