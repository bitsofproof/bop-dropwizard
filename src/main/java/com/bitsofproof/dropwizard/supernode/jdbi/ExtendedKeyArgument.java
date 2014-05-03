package com.bitsofproof.dropwizard.supernode.jdbi;

import com.bitsofproof.supernode.common.ExtendedKey;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ExtendedKeyArgument implements Argument
{
	private final ExtendedKey extendedKey;

	public ExtendedKeyArgument (ExtendedKey value)
	{
		extendedKey = value;
	}

	@Override
	public void apply (int position, PreparedStatement statement, StatementContext ctx) throws SQLException
	{
		if ( extendedKey != null )
		{
			statement.setString (position, extendedKey.serialize (true));
		}
		else
		{
			statement.setNull (position, Types.VARCHAR);
		}
	}
}
