package com.bitsofproof.security.shiro.authc.credential;

import com.bitsofproof.security.shiro.authc.TOTPUsernamePasswordToken;
import com.google.authenticator.Base32String;
import com.google.authenticator.TOTP;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.PasswordMatcher;
import org.apache.shiro.codec.CodecException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.util.Objects;

/**
 * A CredentialsMatcher, wich first match the password, then if the user has a TOTP secret AND the request contains a one-time password, the it checks
 * its validity.
 * <p/>
 * Second factor authentication is optional from the authentication perspective. If both the user has a TOTP secret and they provided a one-time
 * password, then authentication
 */
public class TOTPUserPasswordMatcher extends PasswordMatcher
{
	private static final Logger log = LoggerFactory.getLogger ( TOTPUserPasswordMatcher.class );

	/**
	 * After matching the provided password, matches the TOTP checkcode against the provided one-time password only if it is provided, and the user has a
	 * TOTP secret. The lack of the latter means that the user doesn't want second factor authorization.
	 *
	 * @param token
	 * @param info
	 * @return
	 */
	@Override
	public boolean doCredentialsMatch (AuthenticationToken token, AuthenticationInfo info)
	{
		if (!super.doCredentialsMatch ( token, info ))
			return false;

		String totpSecret = getStoredTotpSecret ( info );
		// No TOTP required
		if (totpSecret != null && token instanceof TOTPUsernamePasswordToken)
		{
			String submitedCode = ((TOTPUsernamePasswordToken) token).getTotpToken ();
			if (submitedCode != null)
			{
				try
				{
					return TOTP.verify ( totpSecret, submitedCode );
				}
				catch (GeneralSecurityException e)
				{
					throw new AuthenticationException ( e );
				}
				catch (Base32String.DecodingException e)
				{
					throw new CodecException ( "Error decoding TOTP one-time password", e );
				}
			}

		}

		return true;
	}

	/**
	 * PasswordMatcher assumes that the AuthenticationInfo contains only a simple password string. With two factor authentication we need to store
	 * another credential, so here we extract only the password bit.
	 *
	 * @param storedAccountInfo
	 * @return
	 * @see com.bitsofproof.security.shiro.realm.JdbiShiroRealm
	 */
	protected Object getStoredPassword (AuthenticationInfo storedAccountInfo)
	{
		Object stored = storedAccountInfo != null ? storedAccountInfo.getCredentials () : null;
		//fix for https://issues.apache.org/jira/browse/SHIRO-363
		if (stored instanceof char[])
		{
			stored = new String ( (char[]) stored );
		}
		else if (stored instanceof String[])
		{
			stored = ((String[]) stored)[0];
		}
		return stored;
	}

	protected String getStoredTotpSecret (AuthenticationInfo storedAccountInfo)
	{
		Object stored = storedAccountInfo != null ? storedAccountInfo.getCredentials () : null;
		if (stored instanceof String[] && ((String[]) stored).length == 2)
		{
			return ((String[]) stored)[1];
		}

		return null;
	}
}
