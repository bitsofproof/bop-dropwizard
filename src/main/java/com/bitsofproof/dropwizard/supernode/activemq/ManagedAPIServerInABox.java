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

import java.io.IOException;

import com.bitsofproof.dropwizard.supernode.ManagedBCSAPI;
import com.bitsofproof.supernode.api.BCSAPI;
import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.conf.UnitTestChain;
import com.bitsofproof.supernode.testbox.APIServerInABox;

public class ManagedAPIServerInABox implements ManagedBCSAPI
{

	private final APIServerInABox box;

	public ManagedAPIServerInABox ()
	{
		try
		{
			box = new APIServerInABox (new UnitTestChain ());
		}
		catch ( IOException | ValidationException e )
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

	public APIServerInABox getBox ()
	{
		return box;
	}
}
