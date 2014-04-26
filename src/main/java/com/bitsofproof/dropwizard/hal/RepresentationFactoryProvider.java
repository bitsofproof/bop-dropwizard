package com.bitsofproof.dropwizard.hal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.server.impl.inject.AbstractHttpContextInjectable;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.ws.rs.core.Context;

/**
 * Jersey context provider for RepresentationFactory.
 */
public class RepresentationFactoryProvider implements InjectableProvider<Context, Parameter>
{

	private final ObjectMapper mapper;

	public RepresentationFactoryProvider (ObjectMapper mapper)
	{
		this.mapper = mapper;
	}

	@Override
	public ComponentScope getScope ()
	{
		return ComponentScope.PerRequest;
	}

	@Override
	public Injectable getInjectable (ComponentContext ic, Context context, final Parameter parameter)
	{
		if ( DropwizardRepresentationFactory.class.isAssignableFrom (parameter.getParameterClass ()) )
		{
			return new AbstractHttpContextInjectable<DropwizardRepresentationFactory> ()
			{
				@Override
				public DropwizardRepresentationFactory getValue (HttpContext context)
				{
					try
					{
						DropwizardRepresentationFactory factory = (DropwizardRepresentationFactory) parameter.getParameterClass ().newInstance ();
						factory.setMapper (mapper);
						factory.setUriInfo (context.getUriInfo ());

						return factory;
					}
					catch (InstantiationException | IllegalAccessException e)
					{
						throw new IllegalStateException (e);
					}
				}
			};
		}

		return null;
	}
}
