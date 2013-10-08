package com.bitsofproof.dropwizard.supernode;

import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.BCSAPIException;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Random;

public abstract class SupernodeBundle<T extends Configuration> implements ConfiguredBundle<T>
{

	private ManagedBCSAPI managedBCSAPI;

	protected abstract SupernodeConfiguration getSupernodeConfiguration (T configuration);

	@Override
	public void run (T configuration, Environment environment) throws Exception
	{
		// TODO Add Jackson ObjectMapper module for ExtendedKey mapping
		// also, maybe a DBI mapper too

		final SupernodeConfiguration supernode = getSupernodeConfiguration ( configuration );
		managedBCSAPI = supernode.createBCSAPI ();
		managedBCSAPI.start (); // start it early

		environment.lifecycle ().manage ( managedBCSAPI );
		environment.healthChecks ().register ( "supernode", new HealthCheck ()
		{
			@Override
			protected Result check () throws Exception
			{
				try
				{
					managedBCSAPI.getBCSAPI ().ping ( new Random ().nextLong () );
					return Result.healthy ( "Ping succeeded" );
				}
				catch (BCSAPIException be)
				{
					return Result.unhealthy ( be );
				}

			}
		} );
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
