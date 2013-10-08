package com.bitsofproof.dropwizard.supernode;

import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.api.JMSServerConnector;
import com.google.common.base.Preconditions;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;

import javax.jms.ConnectionFactory;

public class ActiveMQSupernodeConfiguration implements SupernodeConfiguration
{
	private String brokerUrl;

	private String username;

	private String password;

	public ManagedBCSAPI createBCSAPI ()
	{
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory ( username, password, brokerUrl );
		final ConnectionFactory pooledConnectionFactory = new PooledConnectionFactory ( connectionFactory );

		return new ManagedBCSAPI ()
		{
			public JMSServerConnector connector;

			@Override
			public BCSAPI getBCSAPI ()
			{
				Preconditions.checkState ( connector != null, "BCSAPI stopped" );
				return connector;
			}

			@Override
			public void start () throws Exception
			{
				if (connector == null)
				{
					connector = new JMSServerConnector ();
					connector.setConnectionFactory ( pooledConnectionFactory );
					connector.init ();
					connector.isProduction ();
				}
			}

			@Override
			public void stop () throws Exception
			{
				if (connector != null)
				{
					connector.destroy ();
					connector = null;
				}
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
}
