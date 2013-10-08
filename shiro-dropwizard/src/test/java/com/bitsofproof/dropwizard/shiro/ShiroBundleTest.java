package com.bitsofproof.dropwizard.shiro;

import com.google.common.base.Optional;
import io.dropwizard.Configuration;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;

import java.util.EventListener;

import static org.mockito.Mockito.*;

public class ShiroBundleTest
{
	private TestConfig config;

	private Environment environment;

	private Bootstrap<TestConfig> bootstrap;

	private ServletEnvironment servletEnvironment;

	private FilterRegistration.Dynamic filterReg;

	ShiroBundle<TestConfig> createBundle ()
	{
		return new ShiroBundle<TestConfig> ()
		{
			@Override
			public Optional<ShiroConfiguration> getShiroConfiguration (TestConfig configuration)
			{
				return Optional.fromNullable ( config.shiroConfig );
			}
		};
	}

	@Before
	public void setUp ()
	{
		config = new TestConfig ();
		bootstrap = mock ( Bootstrap.class );

		filterReg = mock ( FilterRegistration.Dynamic.class );
		servletEnvironment = mock ( ServletEnvironment.class );
		when ( servletEnvironment.addFilter ( anyString (), Matchers.<Filter>any () ) ).thenReturn ( filterReg );

		environment = mock ( Environment.class );
		when ( environment.servlets () ).thenReturn ( servletEnvironment );
	}

	@Test
	public void testDefaultConfig () throws Exception
	{
		config.shiroConfig.enabled = true;
		ShiroBundle<TestConfig> bundle = createBundle ();

		bundle.initialize ( bootstrap );
		bundle.run ( config, environment );

		verify ( servletEnvironment, atLeastOnce () ).addServletListeners ( Matchers.<EventListener[]>any () );

		verify(servletEnvironment, atLeastOnce ()).addFilter ( anyString (), Matchers.<Filter>any () );
		verify ( filterReg, atLeastOnce () ).addMappingForUrlPatterns ( null, false, config.shiroConfig.getSecuredUrlPattern () );
	}

	@Test
	public void testDisabled () throws Exception
	{
		config.shiroConfig.enabled = false;
		ShiroBundle<TestConfig> bundle = createBundle ();

		bundle.initialize ( bootstrap );
		bundle.run ( config, environment );

		verifyZeroInteractions ( environment );
	}

	@Test
	public void testConfigNotPresent () throws Exception
	{
		config.shiroConfig = null;

		ShiroBundle<TestConfig> bundle = createBundle ();

		bundle.initialize ( bootstrap );
		bundle.run ( config, environment );

		verifyZeroInteractions ( environment );
	}

	private static class TestConfig extends Configuration
	{
		ShiroConfiguration shiroConfig = new ShiroConfiguration ();
	}

}
