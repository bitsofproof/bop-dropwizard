package com.bitsofproof.dropwizard.hal;

import com.theoryinpractise.halbuilder.api.ReadableRepresentation;
import com.theoryinpractise.halbuilder.api.Representation;
import com.theoryinpractise.halbuilder.api.RepresentationFactory;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A MessageBodyWriter for {@link Representation}s. It supports both "application/json" and
 * "application/hal+json" media types.
 */
@Provider
@Produces ({RepresentationFactory.HAL_JSON, MediaType.APPLICATION_JSON})
public class HalMessageBodyWriter implements MessageBodyWriter<Representation>
{
	public static final MediaType HAL_JSON_TYPE = new MediaType ("application", "hal+json");

	public static final MediaType HAL_XML_TYPE = new MediaType ("application", "hal+xml");

	@Override
	public boolean isWriteable (Class aClass, Type type, Annotation[] annotations, MediaType mediaType)
	{
		return ReadableRepresentation.class.isAssignableFrom (aClass) &&
				(mediaType.isCompatible (HAL_JSON_TYPE) || mediaType.isCompatible (HAL_XML_TYPE) || mediaType.isCompatible (MediaType.APPLICATION_JSON_TYPE));
	}

	@Override
	public long getSize (Representation representation, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType)
	{
		return representation.toString (mediaType.toString ()).length ();
	}

	@Override
	public void writeTo (Representation representation, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
	                     MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws
	                                                IOException,
	                                                WebApplicationException
	{
		representation.toString (mediaType.toString (), new OutputStreamWriter (entityStream));
	}

}
