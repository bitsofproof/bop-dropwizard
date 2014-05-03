package com.bitsofproof.dropwizard.supernode.jdbi;

import com.bitsofproof.supernode.common.ExtendedKey;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;
import org.skife.jdbi.v2.tweak.ArgumentFactory;

public class ExtendedKeyArgumentFactory implements ArgumentFactory<ExtendedKey> {
	@Override
	public boolean accepts (Class<?> expectedType, Object value, StatementContext ctx)
	{
		return value instanceof ExtendedKey;
	}

	@Override
	public Argument build (Class<?> expectedType, ExtendedKey value, StatementContext ctx)
	{
		return new ExtendedKeyArgument(value);
	}
}
