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
