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
