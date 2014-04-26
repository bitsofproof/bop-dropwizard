package com.bitsofproof.dropwizard.hal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;
import com.theoryinpractise.halbuilder.json.JsonRepresentationWriter;

import java.net.URI;
import java.util.Set;

/**
 * Same as the JsonRepresentationWriter, except it uses the injected and configured ObjectMapper.
 */
public class DropwizardRepresentationWriter extends JsonRepresentationWriter
{
	private ObjectMapper mapper;

	public void setObjectMapper (ObjectMapper mapper)
	{
		this.mapper = mapper;
	}

	protected ObjectMapper getObjectMapper()
	{
		return (mapper == null) ? new ObjectMapper() : mapper.copy();
	}

	@Override
	protected JsonFactory getJsonFactory (Set<URI> flags)
	{
		JsonFactory f = new JsonFactory();
		ObjectMapper codec = getObjectMapper();
		if (flags.contains(RepresentationFactory.STRIP_NULLS)) {
			codec.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		}
		//if SINGLE_ELEM_ARRAYS is set, write arrays with one element as an array
		//rather than a single value.
		codec.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,
		                !flags.contains(RepresentationFactory.SINGLE_ELEM_ARRAYS));
		f.setCodec(codec);
		f.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

		return f;
	}
}
