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
