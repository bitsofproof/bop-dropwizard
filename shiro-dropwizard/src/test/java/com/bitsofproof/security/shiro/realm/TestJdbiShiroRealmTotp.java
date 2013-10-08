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
