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

package com.bitsofproof.dropwizard.supernode;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.dropwizard.supernode.activemq.ManagedAPIServerInABox;
import com.bitsofproof.dropwizard.supernode.jackson.SupernodeModule;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.bitsofproof.supernode.testbox.APIServerInABox;
import com.codahale.metrics.health.HealthCheck;

public abstract class SupernodeBundle<T extends Configuration> implements ConfiguredBundle<T>
{
	private static final Logger log = LoggerFactory.getLogger (SupernodeBundle.class);

	private ManagedBCSAPI managedBCSAPI;

	protected abstract SupernodeConfiguration getSupernodeConfiguration (T configuration);

	@Override
	public void run (T configuration, Environment environment) throws Exception
	{
		// jackson module for JSON serialization
		environment.getObjectMapper ().registerModule (new SupernodeModule ());

		final SupernodeConfiguration supernode = getSupernodeConfiguration (configuration);

		log.info ("Creating BCSAPI instance");
		managedBCSAPI = supernode.createBCSAPI ();
		log.info ("Starting BCSAPI instance");
		managedBCSAPI.start (); // start it early

		environment.lifecycle ().manage (managedBCSAPI);
		environment.healthChecks ().register ("supernode", new HealthCheck ()
		{
			@Override
			protected Result check () throws Exception
			{
				try
				{
					managedBCSAPI.getBCSAPI ().ping (new Random ().nextLong ());
					return Result.healthy ("Ping succeeded");
				}
				catch ( BCSAPIException be )
				{
					return Result.unhealthy (be);
				}

			}
		});
	}

	public BCSAPI getBCSAPI ()
	{
		return managedBCSAPI.getBCSAPI ();
	}

	public APIServerInABox getBox ()
	{
		if ( managedBCSAPI instanceof ManagedAPIServerInABox )
		{
			return ((ManagedAPIServerInABox) managedBCSAPI).getBox ();
		}
		return null;
	}

	@Override
	public void initialize (Bootstrap<?> bootstrap)
	{
	}
}
