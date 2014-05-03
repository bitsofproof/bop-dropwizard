package com.bitsofproof.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class Basic2FAAuthProviderTest extends JerseyTest
{
	static
	{
		LoggingFactory.bootstrap ();
	}

	@Path ("/test/")
	@Produces (MediaType.TEXT_PLAIN)
	public static final class TestResource
	{

		@GET
		@Path ("/anon")
		public String anon ()
		{
			return "anon";
		}

		@GET
		@Path ("/optional")
		public String optional (@RestrictedTo (required = false) String user)
		{
			return user == null ? "anon" : user;
		}

		@GET
		@Path ("/required")
		public String required (@RestrictedTo String user)
		{
			return user;
		}

		@GET
		@Path ("/secondFactor")
		public String secondFactor (@RestrictedTo (required = true, secondFactor = true) String user)
		{
			return user;
		}
	}

	@Override
	protected AppDescriptor configure ()
	{
		final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting (new MetricRegistry ());
		final Authenticator<Basic2FACredentials, String> authenticator = new Authenticator<Basic2FACredentials, String> ()
		{
			@Override
			public Optional<String> authenticate (Basic2FACredentials credentials) throws AuthenticationException
			{
				if ( credentials.getSecondFactor ().isPresent () && !"1".equals (credentials.getSecondFactor ().get ()) )
				{
					return Optional.absent ();
				}

				if ( "good-guy".equals (credentials.getUsername ()) &&
						Arrays.equals ("secret".toCharArray (), credentials.getPassword ()))
				{
					return Optional.of ("good-guy");
				}

				if ( "bad-guy".equals (credentials.getUsername ()) )
				{
					throw new AuthenticationException ("CRAP");
				}

				return Optional.absent ();
			}
		};

		config.getSingletons ().add (new Basic2FAAuthProvider<> (authenticator, "realm"));
		config.getSingletons ().add (new TestResource ());
		return new LowLevelAppDescriptor.Builder (config).build ();
	}

	@Test
	public void respondsToMissingCredentialsWith401 () throws Exception
	{
		try
		{
			client ().resource ("/test/required").get (String.class);
			failBecauseExceptionWasNotThrown (UniformInterfaceException.class);
		}
		catch (UniformInterfaceException e)
		{
			assertThat (e.getResponse ().getStatus ()).isEqualTo (401);
			assertThat (e.getResponse ().getHeaders ().get (HttpHeaders.WWW_AUTHENTICATE))
					.containsOnly ("Basic realm=\"realm\"");
		}
	}

	@Test
	public void transformsCredentialsToPrincipals () throws Exception
	{
		assertThat (client ().resource ("/test/required")
				                       .header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
				                       .get (String.class))
				.isEqualTo ("good-guy");
	}

	@Test
	public void responseToNonBasicCredentialsWith401 () throws Exception
	{
		try
		{
			client ().resource ("/test/required")
					.header (HttpHeaders.AUTHORIZATION, "Derp Z29vZC1ndXk6c2VjcmV0")
					.get (String.class);
			failBecauseExceptionWasNotThrown (UniformInterfaceException.class);
		}
		catch (UniformInterfaceException e)
		{
			assertThat (e.getResponse ().getStatus ()).isEqualTo (401);
			assertThat (e.getResponse ().getHeaders ().get (HttpHeaders.WWW_AUTHENTICATE))
					.containsOnly ("Basic realm=\"realm\"");
		}
	}

	@Test
	public void responseToExceptionsWith500 () throws Exception
	{
		try
		{
			client ().resource ("/test/required")
					.header (HttpHeaders.AUTHORIZATION, "Basic YmFkLWd1eTpzZWNyZXQ=")
					.get (String.class);
			failBecauseExceptionWasNotThrown (UniformInterfaceException.class);
		}
		catch (UniformInterfaceException e)
		{
			assertThat (e.getResponse ().getStatus ()).isEqualTo (500);
		}
	}

	@Test
	public void optionalAuthenticationShouldNotReturn401 () throws Exception
	{
		assertThat (client ().resource ("/test/optional").get (String.class)).isEqualTo ("anon");
		assertThat (client ().resource ("/test/optional")
				                       .header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
				                       .get (String.class))
				.isEqualTo ("good-guy");
	}

	@Test
	public void anonymousAccessShouldNotReturn401 () throws Exception
	{
		assertThat (client ().resource ("/test/anon").get (String.class)).isEqualTo ("anon");
		assertThat (client ().resource ("/test/anon")
				                       .header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
				                       .get (String.class))
				.isEqualTo ("anon");
	}

	@Test
	public void missingSecondFactorShouldReturn401 () throws Exception
	{
		try
		{
			client ().resource ("/test/secondFactor")
					.header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
					.get (String.class);
			failBecauseExceptionWasNotThrown (UniformInterfaceException.class);
		}
		catch (UniformInterfaceException e)
		{
			assertThat (e.getResponse ().getStatus ()).isEqualTo (401);
			assertThat (e.getResponse ().getHeaders ().get (HttpHeaders.WWW_AUTHENTICATE))
					.containsOnly ("Basic realm=\"realm\"");
			assertThat (e.getResponse ().getHeaders ()).containsEntry ("X-BOP-OTP", Collections.singletonList ("required"));
		}
	}

	@Test
	public void badSecondFactorShouldReturn401 () throws Exception
	{
		try
		{
			client ().resource ("/test/secondFactor")
					.header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
					.header ("X-BOP-OTP", "2")
					.get (String.class);
			failBecauseExceptionWasNotThrown (UniformInterfaceException.class);
		}
		catch (UniformInterfaceException e)
		{
			assertThat (e.getResponse ().getStatus ()).isEqualTo (401);
			assertThat (e.getResponse ().getHeaders ().get (HttpHeaders.WWW_AUTHENTICATE))
					.containsOnly ("Basic realm=\"realm\"");
			assertThat (e.getResponse ().getHeaders ()).containsEntry ("X-BOP-OTP", Collections.singletonList ("required"));
		}
	}

	@Test
	public void goodSecondFactorTransformsCredentialsToPrincipals () throws Exception
	{
		assertThat (client ().resource ("/test/secondFactor")
				                       .header (HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
				                       .header ("X-BOP-OTP", "1")
				                       .get (String.class))
				.isEqualTo ("good-guy");
	}
}

