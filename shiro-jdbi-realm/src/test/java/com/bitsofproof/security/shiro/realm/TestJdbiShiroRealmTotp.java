/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.bitsofproof.security.shiro.realm;

import com.bitsofproof.security.shiro.SecondFactorPrincipal;
import com.bitsofproof.security.shiro.authc.TOTPUsernamePasswordToken;
import com.bitsofproof.security.shiro.dao.jdbi.DefaultUserImpl;
import com.google.authenticator.Base32String;
import com.google.authenticator.TOTP;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing TOTP functionality
 */
public class TestJdbiShiroRealmTotp extends TestJdbiShiroRealm
{
	private static final Logger log = LoggerFactory.getLogger ( TestJdbiShiroRealmTotp.class );

	private static String totpSecret;

	@Override
	protected Logger log ()
	{
		return log;
	}

	@BeforeClass
	public static void setupTOTP ()
	{
		totpSecret = TOTP.generateSecret ();
	}

	protected String getTotpSecret ()
	{
		return totpSecret;
	}

	protected String getTotpToken ()
	{
		try
		{
			return TOTP.getCheckCode ( totpSecret );
		}
		catch (GeneralSecurityException | Base32String.DecodingException e)
		{
			throw new RuntimeException ( e );
		}
	}

	@Test
	public void testNoTOTPToken ()
	{
		DefaultUserImpl u = fetchOrCreateUser ();

		TOTPUsernamePasswordToken upToken = new TOTPUsernamePasswordToken ( u.getUsername (), getPlainTextPassword () );
		AuthenticationInfo authNInfo = realm.doGetAuthenticationInfo ( upToken );

		AuthorizationInfo authZInfo = realm.doGetAuthorizationInfo ( authNInfo.getPrincipals () );
		Collection<String> rolesC = authZInfo.getRoles ();
		assertTrue ( "Expect different number of roles.", rolesC.size () == getRoles ().size () );
		assertTrue ( "Expect different role to be assigned.", rolesC.contains ( getRoles ().iterator ().next ().getName () ) );

		List<String> ls = Lists.newArrayList ( getExpectedPermissions () );
		ls.remove ( SecondFactorPrincipal.PERMISSION );

		checkExpectedPermissions ( ls.toArray ( new String[]{} ), authZInfo.getStringPermissions () );
	}

	@Test(expected = AuthenticationException.class)
	public void testIncorrectTOTPCode ()
	{
		DefaultUserImpl u = fetchOrCreateUser ();
		// This is what would be provided on login with the wrong password.
		TOTPUsernamePasswordToken upToken = new TOTPUsernamePasswordToken ( u.getUsername (), getPlainTextPassword ());
		upToken.setTotpToken ( "WrOnG" );
		getSecurityManager ().authenticate ( upToken );

	}

	protected String getUsername ()
	{
		return "TOTP-test";
	}

	// We expect secondary principal values are optional and not used for authZ functionality that is under test.
	// This class tests that.
	@Override
	protected void configureRealm (JdbiShiroRealm realm)
	{
		//realm.setPrincipalValueFields ( Arrays.asList ( JdbiShiroRealm.PrincipalValueField.USER_ID, JdbiShiroRealm.PrincipalValueField.USERNAME ) );
	}
}
