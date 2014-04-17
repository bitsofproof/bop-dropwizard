/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bitsofproof.dropwizard.supernode;

import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.dropwizard.supernode.jackson.SupernodeModule;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
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
		try
		{
			log.info ("Starting BCSAPI instance");
			managedBCSAPI.start (); // start it early
		}
		catch ( IllegalStateException e )
		{
			log.warn ("BCSAPI not initialized"); // TODO: this is temp fix until testbox integrated
		}
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

	@Override
	public void initialize (Bootstrap<?> bootstrap)
	{
	}
}
