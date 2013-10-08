package com.bitsofproof.security.shiro.authc;

import org.apache.shiro.authc.UsernamePasswordToken;

public class TOTPUsernamePasswordToken extends UsernamePasswordToken
{
	private String totpToken;

	public TOTPUsernamePasswordToken ()
	{
		super ();
	}

	public TOTPUsernamePasswordToken (String username, char[] password)
	{
		super ( username, password );
	}

	public TOTPUsernamePasswordToken (String username, String password)
	{
		super ( username, password );
	}

	public TOTPUsernamePasswordToken (String username, char[] password, String host)
	{
		super ( username, password, host );
	}

	public TOTPUsernamePasswordToken (String username, String password, String host)
	{
		super ( username, password, host );
	}

	public TOTPUsernamePasswordToken (String username, char[] password, boolean rememberMe)
	{
		super ( username, password, rememberMe );
	}

	public TOTPUsernamePasswordToken (String username, String password, boolean rememberMe)
	{
		super ( username, password, rememberMe );
	}

	public TOTPUsernamePasswordToken (String username, char[] password, boolean rememberMe, String host)
	{
		super ( username, password, rememberMe, host );
	}

	public TOTPUsernamePasswordToken (String username, String password, boolean rememberMe, String host)
	{
		super ( username, password, rememberMe, host );
	}

	@Override
	public void clear ()
	{
		super.clear ();
		totpToken = null;
	}

	public String getTotpToken ()
	{
		return totpToken;
	}

	public void setTotpToken (String totpToken)
	{
		this.totpToken = totpToken;
	}
}
