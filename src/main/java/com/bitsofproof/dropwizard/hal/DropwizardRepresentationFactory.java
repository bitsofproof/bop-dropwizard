package com.bitsofproof.dropwizard.hal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.theoryinpractise.halbuilder.DefaultRepresentationFactory;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.api.RepresentationWriter;
import com.theoryinpractise.halbuilder.json.JsonRepresentationReader;

import javax.ws.rs.core.UriInfo;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * RepresentationFactory which uses a pre-configured ObjectMapper instance.
 * <p/>
 * It also add renderer for "application/json" media type.
 */
public class DropwizardRepresentationFactory extends DefaultRepresentationFactory
{
	private ObjectMapper mapper;

	protected UriInfo uriInfo;

	public DropwizardRepresentationFactory ()
	{
		withRenderer (HAL_JSON, DropwizardRepresentationWriter.class);
		withRenderer (APPLICATION_JSON, DropwizardRepresentationWriter.class);
		withReader (HAL_JSON, JsonRepresentationReader.class);
	}

	public void setMapper (ObjectMapper mapper)
	{
		this.mapper = mapper;
	}

	public void setUriInfo (UriInfo uriInfo)
	{
		this.uriInfo = uriInfo;
	}

	public UriInfo getUriInfo ()
	{
		return uriInfo;
	}

	@Override
	public RepresentationWriter<String> lookupRenderer (String contentType)
	{
		RepresentationWriter<String> renderer = super.lookupRenderer (contentType);
		if ( renderer instanceof DropwizardRepresentationWriter )
		{
			((DropwizardRepresentationWriter) renderer).setObjectMapper (mapper);
		}
		return renderer;
	}
}
