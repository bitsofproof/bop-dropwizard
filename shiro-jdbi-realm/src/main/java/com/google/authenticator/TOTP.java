package com.google.authenticator;

import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.authenticator.Base32String.DecodingException;

public class TOTP
{
	public static final SecureRandom rng = new SecureRandom ();

	public static String generateSecret ()
	{
		byte[] secret = new byte[10];
		rng.nextBytes ( secret );
		return Base32String.encode ( secret );

	}

	public static String getCheckCode (String secret) throws GeneralSecurityException, DecodingException
	{
		PasscodeGenerator pcg = getPasscodeGenerator ( secret );
		return pcg.generateResponseCode ( System.currentTimeMillis () / 30000L );
	}

	public static boolean verify (String secret, String timeoutCode) throws GeneralSecurityException, DecodingException
	{
		return getPasscodeGenerator ( secret ).verifyTimeoutCode ( System.currentTimeMillis () / 30000L, timeoutCode );
	}

	private static PasscodeGenerator getPasscodeGenerator (String secret) throws DecodingException, NoSuchAlgorithmException,
	                                                                             InvalidKeyException
	{
		final byte[] keyBytes = Base32String.decode ( secret );
		Mac mac = Mac.getInstance ( "HMACSHA1" );
		mac.init ( new SecretKeySpec ( keyBytes, "" ) );
		return new PasscodeGenerator ( mac );
	}
}
