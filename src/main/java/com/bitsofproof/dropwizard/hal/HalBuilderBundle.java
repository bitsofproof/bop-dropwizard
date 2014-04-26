package com.bitsofproof.dropwizard.hal;

import io.dropwizard.Bundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

public class HalBuilderBundle implements Bundle
{
	@Override
	public void initialize (Bootstrap<?> bootstrap)
	{
	}

	@Override
	public void run (Environment environment)
	{
		environment.jersey ().register (new RepresentationFactoryProvider (environment.getObjectMapper ()));
		environment.jersey ().register (new HalMessageBodyWriter ());
	}
}
