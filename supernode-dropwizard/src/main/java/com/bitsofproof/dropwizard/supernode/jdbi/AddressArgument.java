package com.bitsofproof.dropwizard.supernode.jdbi;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;

public class AddressArgument implements Argument
{
	private final Address address;

	public AddressArgument (Address value)
	{
		this.address = value;
	}

	@Override
	public void apply (int position, PreparedStatement statement, StatementContext ctx) throws SQLException
	{
		try
		{
			if ( address != null )
			{
				statement.setString (position, Address.toSatoshiStyle (address));
			}
			else
			{
				statement.setNull (position, Types.VARCHAR);
			}
		}
		catch ( ValidationException e )
		{
			throw new IllegalStateException (e);
		}
	}
}
