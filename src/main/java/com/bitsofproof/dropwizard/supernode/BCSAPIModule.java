package com.bitsofproof.dropwizard.supernode;

import com.bitsofproof.supernode.conf.SupernodeModule;
import com.bitsofproof.supernode.core.BCSAPIServer;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonSubTypes ({ @JsonSubTypes.Type (BCSAPIModule.EmbeddedBCSAPIModule.class) })
@JsonTypeInfo (use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public abstract class BCSAPIModule extends SupernodeModule
{
	@JsonTypeName ("embedded")
	public static class EmbeddedBCSAPIModule extends BCSAPIModule
	{
		@Override
		protected void configure ()
		{
			bind (BCSAPIServer.class).asEagerSingleton ();
		}
	}
}
