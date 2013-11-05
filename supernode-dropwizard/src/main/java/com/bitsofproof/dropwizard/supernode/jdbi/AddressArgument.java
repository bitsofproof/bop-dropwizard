package com.bitsofproof.dropwizard.supernode.jdbi;

import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.Address;
import com.bitsofproof.supernode.wallet.AddressConverter;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.Argument;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

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
			if (address != null)
			{
				statement.setString (position, AddressConverter.toSatoshiStyle (address));
			}
			else
			{
				statement.setNull (position, Types.VARCHAR);
			}
		}
		catch (ValidationException e)
		{
			throw new IllegalStateException (e);
		}
	}
}
