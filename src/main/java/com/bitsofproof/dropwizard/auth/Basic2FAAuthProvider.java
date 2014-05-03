package com.bitsofproof.dropwizard.auth;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import org.eclipse.jetty.util.B64Code;
import org.eclipse.jetty.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Basic2FAAuthProvider<T> implements InjectableProvider<RestrictedTo, Parameter>
{
	private static final Logger log = LoggerFactory.getLogger (Basic2FAAuthProvider.class);

	public static final String OTP_HEADER = "X-BOP-OTP";

	private static class Basic2FAAuthInjectable<T> extends AbstractHttpContextInjectable<T>
	{
		private static final String BASIC_PREFIX = "Basic";

		private static final String CHALLENGE_FORMAT = BASIC_PREFIX + " realm=\"%s\"";

		private final Authenticator<Basic2FACredentials, T> authenticator;

		private final String realm;

		private final boolean required;

		private final boolean secondFactor;

		private Basic2FAAuthInjectable (Authenticator<Basic2FACredentials, T> authenticator,
		                                String realm,
		                                boolean required,
		                                boolean secondFactor)
		{
			this.authenticator = authenticator;
			this.realm = realm;
			this.required = required;
			this.secondFactor = secondFactor;
		}

		@Override
		public T getValue (HttpContext c)
		{
			final Optional<String> secondFactorChallenge = secondFactor ? extract2FA (c) : Optional.<String>absent ();
			final String header = c.getRequest ().getHeaderValue (HttpHeaders.AUTHORIZATION);
			try
			{
				if ( header != null && (!secondFactor || secondFactorChallenge.isPresent ()) )
				{
					final int space = header.indexOf (' ');
					if ( space > 0 )
					{
						final String method = header.substring (0, space);
						if ( BASIC_PREFIX.equalsIgnoreCase (method) )
						{
							final String decoded = B64Code.decode (header.substring (space + 1),
							                                       StringUtil.__ISO_8859_1);
							final int i = decoded.indexOf (':');
							if ( i > 0 )
							{
								final String username = decoded.substring (0, i);
								final char[] password = decoded.substring (i + 1).toCharArray ();
								final Basic2FACredentials credentials = new Basic2FACredentials (username,
								                                                                 password,
								                                                                 secondFactorChallenge);

								final Optional<T> result = authenticator.authenticate (credentials);
								if ( result.isPresent () )
								{
									return result.get ();
								}
							}
						}
					}
				}
			}
			catch (IllegalArgumentException e)
			{
				log.debug ("Error decoding credentials", e);
			}
			catch (AuthenticationException e)
			{
				log.warn ("Error authenticating credentials", e);
				throw new WebApplicationException (Response.Status.INTERNAL_SERVER_ERROR);
			}

			if ( required )
			{
				final String challenge = String.format (CHALLENGE_FORMAT, realm);
				final Response.ResponseBuilder responseBuilder =
						Response.status (Response.Status.UNAUTHORIZED)
						        .header (javax.ws.rs.core.HttpHeaders.WWW_AUTHENTICATE, challenge)
						        .entity ("Credentials are required to access this resource.")
						        .type (MediaType.TEXT_PLAIN_TYPE);
				if ( secondFactor )
				{
					responseBuilder.header (OTP_HEADER, "required");
				}
				throw new WebApplicationException (responseBuilder.build ());
			}

			return null;
		}

		private Optional<String> extract2FA (HttpContext c)
		{
			final String otpHeader = c.getRequest ().getHeaderValue (OTP_HEADER);

			return Optional.fromNullable (Strings.emptyToNull (otpHeader));
		}
	}

	private final Authenticator<Basic2FACredentials, T> authenticator;

	private final String realm;

	public Basic2FAAuthProvider (Authenticator<Basic2FACredentials, T> authenticator, String realm)
	{
		this.authenticator = authenticator;
		this.realm = realm;
	}

	@Override
	public ComponentScope getScope ()
	{
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable<?> getInjectable (ComponentContext ic, RestrictedTo a, Parameter parameter)
	{
		return new Basic2FAAuthInjectable<> (authenticator, realm, a.required (), a.secondFactor ());
	}
}
