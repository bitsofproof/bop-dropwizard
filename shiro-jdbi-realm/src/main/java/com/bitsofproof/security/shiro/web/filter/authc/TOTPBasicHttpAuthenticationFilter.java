package com.bitsofproof.security.shiro.web.filter.authc;

import com.bitsofproof.security.shiro.authc.TOTPUsernamePasswordToken;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.BasicHttpAuthenticationFilter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class TOTPBasicHttpAuthenticationFilter extends BasicHttpAuthenticationFilter
{
	@Override
	protected AuthenticationToken createToken (String username, String password, ServletRequest request, ServletResponse response)
	{
		boolean rememberMe = isRememberMe(request);
		String host = getHost(request);

		TOTPUsernamePasswordToken token = new TOTPUsernamePasswordToken ( username, password, rememberMe, host );

		String[] secret = request.getParameterValues ( "totpToken" );
		if (secret != null && secret.length > 0)
		{
			token.setTotpToken ( secret[0] );
		}

		return token;
	}
}
