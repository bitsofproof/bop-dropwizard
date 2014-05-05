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

import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.conf.DiscoveryModule;
import com.bitsofproof.supernode.conf.NetworkModule;
import com.bitsofproof.supernode.conf.StoreModule;
import com.bitsofproof.supernode.connector.BCSAPIClient;
import com.bitsofproof.supernode.connector.ConnectorFactory;
import com.bitsofproof.supernode.connector.InMemoryConnectorFactory;
import com.bitsofproof.supernode.core.BitcoinNetwork;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.mycila.guice.ext.closeable.CloseableModule;
import com.mycila.guice.ext.jsr250.Jsr250Module;

public class EmbeddedSupernode implements SupernodeConfiguration
{
	@JsonProperty
	private NetworkModule network;

	@JsonProperty
	private StoreModule store;

	@JsonProperty
	private DiscoveryModule discovery;

	@JsonProperty
	private BCSAPIModule bcsapi;

	@JsonProperty
	private Stage environment;

	private final ConnectorFactory connectorFactory = new InMemoryConnectorFactory ();

	public class SupernodeConfigurationModule extends AbstractModule
	{
		@Override
		protected void configure ()
		{
			bind (ConnectorFactory.class).toInstance (connectorFactory);

			install (network);
			install (store);
			install (discovery);
			install (bcsapi);
		}
	}

	@Override
	public ManagedBCSAPI createBCSAPI ()
	{
		final BCSAPIClient api = new BCSAPIClient ();

		return new ManagedBCSAPI ()
		{
			@Override
			public void start () throws Exception
			{
				Injector injector = Guice.createInjector (environment,
						new CloseableModule (),
						new Jsr250Module (),
						new EmbeddedSupernode.SupernodeConfigurationModule ());

				BitcoinNetwork network = injector.getInstance (BitcoinNetwork.class);
				if ( network.getStore ().isEmpty () )
				{
					network.getStore ().resetStore (network.getChain ());
				}

				network.getStore ().cache (network.getChain (), 0);
				network.start ();

				while ( network.getStore ().isLoading () )
				{
					Thread.sleep (1000);
				}

				api.setConnectionFactory (connectorFactory);
				api.init ();
			}

			@Override
			public void stop () throws Exception
			{
			}

			@Override
			public BCSAPI getBCSAPI ()
			{
				return api;
			}
		};
	}
}
