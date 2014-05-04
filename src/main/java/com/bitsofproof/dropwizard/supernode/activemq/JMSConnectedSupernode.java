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

import org.hibernate.validator.constraints.NotEmpty;

import com.bitsofproof.dropwizard.supernode.ManagedBCSAPI;
import com.bitsofproof.dropwizard.supernode.SupernodeConfiguration;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.connector.BCSAPIClient;
import com.bitsofproof.supernode.jms.JMSConnectorFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

public class JMSConnectedSupernode implements SupernodeConfiguration
{
	@JsonProperty
	@NotEmpty
	private String brokerUrl;

	@JsonProperty
	private String username;

	@JsonProperty
	private String password;

	@Override
	public ManagedBCSAPI createBCSAPI ()
	{
		final BCSAPIClient api = new BCSAPIClient ();
		return new ManagedBCSAPI ()
		{
			@Override
			public void start () throws Exception
			{
				api.setConnectionFactory (new JMSConnectorFactory (username, password, brokerUrl));
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

	public String getBrokerUrl ()
	{
		return brokerUrl;
	}

	public void setBrokerUrl (String brokerUrl)
	{
		this.brokerUrl = brokerUrl;
	}

	public String getUsername ()
	{
		return username;
	}

	public void setUsername (String username)
	{
		this.username = username;
	}

	public String getPassword ()
	{
		return password;
	}

	public void setPassword (String password)
	{
		this.password = password;
	}

	@Override
	public void init ()
	{
	}
}
