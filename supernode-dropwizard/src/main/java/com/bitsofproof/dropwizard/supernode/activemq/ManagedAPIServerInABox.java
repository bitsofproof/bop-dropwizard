package com.bitsofproof.dropwizard.supernode.activemq;

import com.bitsofproof.dropwizard.supernode.ManagedBCSAPI;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.core.UnitTestChain;
import com.bitsofproof.supernode.testbox.APIServerInABox;
import io.dropwizard.lifecycle.Managed;

import java.io.IOException;

public class ManagedAPIServerInABox implements ManagedBCSAPI
{

	private final APIServerInABox box;

	public ManagedAPIServerInABox ()
	{
		try
		{
			box = new APIServerInABox (new UnitTestChain());
		}
		catch (IOException | ValidationException e)
		{
			throw new RuntimeException (e);
		}
	}

	@Override
	public void start () throws Exception
	{
	}

	@Override
	public void stop () throws Exception
	{
	}

	@Override
	public BCSAPI getBCSAPI ()
	{
		return box.getAPI ();
	}

	public APIServerInABox getBox()
	{
		return box;
	}
}
