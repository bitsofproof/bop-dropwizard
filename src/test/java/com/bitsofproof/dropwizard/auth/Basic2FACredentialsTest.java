package com.bitsofproof.dropwizard.auth;

import com.google.common.base.Optional;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class Basic2FACredentialsTest
{
	@Test
	public void testEquals ()
	{
		assertEquals (new Basic2FACredentials ("user", "password".toCharArray(), Optional.<String>absent ()),
		              new Basic2FACredentials ("user", "password".toCharArray(), Optional.<String>absent ()));
		assertEquals (new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("1")),
		              new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("1")));

		assertNotEquals (new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("1")),
		                 new Basic2FACredentials ("user", "password2".toCharArray(), Optional.of ("1")));

		assertNotEquals (new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("1")),
		                 new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("2")));

		assertNotEquals (new Basic2FACredentials ("user", "password".toCharArray(), Optional.<String>absent ()),
		                 new Basic2FACredentials ("user", "password".toCharArray(), Optional.of ("1")));
	}


}
