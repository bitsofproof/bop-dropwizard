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
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.shiro.web.env.EnvironmentLoaderListener;
import org.apache.shiro.web.servlet.ShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple bundle class to initialze Shiro within Dropwizard.
 */
public abstract class ShiroBundle<T extends Configuration> implements ConfiguredBundle<T>, ConfigurationStrategy<T>
{

	private static final Logger log = LoggerFactory.getLogger ( ShiroBundle.class );

	/**
	 * This method is a no-op. All functionality is in the {@link #run(Object, Environment)} method.
	 *
	 * @param bootstrap ignored
	 */
	@Override
	public void initialize (Bootstrap<?> bootstrap)
	{
		// nothing to see here, all the action takes place from the run() method.
	}

	/**
	 * Conditionally configures Dropwizard's environment to enable Shiro elements. The condition being: if there is a ShiroConfiguration present in the
	 * provided {@code configuration}, and its {@code enabled} field is set to {@code true}.
	 *
	 * @param configuration used to retrieve the (optional) {@link ShiroConfiguration} instance.
	 * @param environment   this is what gets configured
	 * @throws Exception
	 */
	@Override
	public void run (final T configuration, Environment environment) throws Exception
	{
		final Optional<ShiroConfiguration> shiroConfig = getShiroConfiguration ( configuration );
		if (shiroConfig.isPresent ())
		{
			log.debug ( "Shiro is configured: {}", shiroConfig );
			initializeShiro ( shiroConfig.get (), environment );
		}
		else
		{
			log.debug ( "Shiro is not configured" );
		}
	}

	private void initializeShiro (final ShiroConfiguration config, Environment environment)
	{
		if (config.isEnabled ())
		{
			log.debug ( "Shiro is enabled" );

			// This line ensure Shiro is configured and its .ini file found in the designated location.
			// e.g., via the shiroConfigLocations ContextParameter with fall-backs to default locations if that parameter isn't specified.
			environment.servlets ().addServletListeners ( new EnvironmentLoaderListener () );

			final String filterUrlPattern = config.getSecuredUrlPattern ();
			log.debug ( "ShiroFilter will check URLs matching '{}'.", filterUrlPattern );
			environment.servlets ()
			           .addFilter ( "shiro-filter", new ShiroFilter () )
			           .addMappingForUrlPatterns ( null, false, filterUrlPattern );
		}
		else
		{
			log.debug ( "Shiro is not enabled" );
		}
	}

}