package com.bitsofproof.dropwizard.supernode;

import com.bitsofproof.supernode.api.BCSAPI;
import io.dropwizard.lifecycle.Managed;

public interface ManagedBCSAPI extends Managed
{
	BCSAPI getBCSAPI();
}
