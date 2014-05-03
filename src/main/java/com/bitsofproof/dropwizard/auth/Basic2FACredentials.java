package com.bitsofproof.dropwizard.auth;

import com.google.common.base.Optional;

import java.util.Objects;

public class Basic2FACredentials
{
	private final String username;

	private final char[] password;

	private final Optional<String> secondFactor;

	public Basic2FACredentials (String username, char[] password, Optional<String> secondFactor)
	{
		this.username = username;
		this.password = password;
		this.secondFactor = secondFactor;
	}

	public String getUsername ()
	{
		return username;
	}

	public char[] getPassword ()
	{
		return password;
	}

	public Optional<String> getSecondFactor ()
	{
		return secondFactor;
	}

	@Override
	public int hashCode ()
	{
		return Objects.hash (username, password, secondFactor);
	}

	@Override
	public boolean equals (Object obj)
	{
		if ( this == obj )
		{
			return true;
		}
		if ( obj == null || getClass () != obj.getClass () )
		{
			return false;
		}
		final Basic2FACredentials other = (Basic2FACredentials) obj;

		boolean result = isEqual (this.username.toCharArray (), other.username.toCharArray ())
				&& isEqual (this.password, other.password);

		if ( this.secondFactor.isPresent () && other.secondFactor.isPresent () )
		{
			result &= isEqual(this.secondFactor.get().toCharArray (), other.secondFactor.get().toCharArray ());
		}
		else
		{
			result &= (this.secondFactor.isPresent () == other.secondFactor.isPresent ());
		}

		return result;
	}

	public static boolean isEqual (char[] a, char[] b)
	{
		if ( a.length != b.length )
		{
			return false;
		}

		int result = 0;
		for (int i = 0; i < a.length; i++)
		{
			result |= a[i] ^ b[i];
		}
		return result == 0;
	}

}
