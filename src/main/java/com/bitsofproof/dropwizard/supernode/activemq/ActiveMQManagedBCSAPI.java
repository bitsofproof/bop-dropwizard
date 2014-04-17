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

package com.bitsofproof.dropwizard.supernode.activemq;

import javax.jms.ConnectionFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bitsofproof.dropwizard.supernode.ManagedBCSAPI;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.jms.JMSServerConnector;
import com.google.common.base.Preconditions;

public class ActiveMQManagedBCSAPI implements ManagedBCSAPI
{
	private static final Logger log = LoggerFactory.getLogger (ActiveMQManagedBCSAPI.class);

	public JMSServerConnector connector;

	private final ConnectionFactory pooledConnectionFactory;

	public ActiveMQManagedBCSAPI (ConnectionFactory pooledConnectionFactory)
	{
		this.pooledConnectionFactory = pooledConnectionFactory;
	}

	@Override
	public BCSAPI getBCSAPI ()
	{
		Preconditions.checkState (connector != null, "BCSAPI stopped");
		return connector;
	}

	@Override
	public void start () throws Exception
	{
		if ( connector == null )
		{
			log.info ("Creating new Supernode server connector JMSServerConnector, implementation of BCSAPI");
			connector = new JMSServerConnector ();
			connector.setConnectionFactory (pooledConnectionFactory);
			connector.init ();
		}
	}

	@Override
	public void stop () throws Exception
	{
		if ( connector != null )
		{
			log.info ("Destroying Supernode server connector");
			connector.destroy ();
			connector = null;
		}
	}
}
