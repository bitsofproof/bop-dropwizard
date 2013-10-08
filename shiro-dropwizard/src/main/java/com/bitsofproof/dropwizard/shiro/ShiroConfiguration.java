/*
 * Copyright 2013 bits of proof zrt.
 *
 * Licensed under the Microsoft Reference Source License (MS-RSL)
 *
 * This license governs use of the accompanying software. If you use the software, you accept this license.
 * If you do not accept the license, do not use the software.
 *
 * 1. Definitions
 * The terms "reproduce," "reproduction," and "distribution" have the same meaning here as under U.S. copyright law.
 * "You" means the licensee of the software.
 * "Your company" means the company you worked for when you downloaded the software.
 * "Reference use" means use of the software within your company as a reference, in read only form, for the sole purposes
 * of debugging your products, maintaining your products, or enhancing the interoperability of your products with the
 * software, and specifically excludes the right to distribute the software outside of your company.
 * "Licensed patents" means any Licensor patent claims which read directly on the software as distributed by the Licensor
 * under this license.
 *
 * 2. Grant of Rights
 * (A) Copyright Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free copyright license to reproduce the software for reference use.
 * (B) Patent Grant- Subject to the terms of this license, the Licensor grants you a non-transferable, non-exclusive,
 * worldwide, royalty-free patent license under licensed patents for reference use.
 *
 * 3. Limitations
 * (A) No Trademark License- This license does not grant you any rights to use the Licensor’s name, logo, or trademarks.
 * (B) If you begin patent litigation against the Licensor over patents that you think may apply to the software
 * (including a cross-claim or counterclaim in a lawsuit), your license to the software ends automatically.
 * (C) The software is licensed "as-is." You bear the risk of using it. The Licensor gives no express warranties,
 * guarantees or conditions. You may have additional consumer rights under your local laws which this license cannot
 * change. To the extent permitted under your local laws, the Licensor excludes the implied warranties of merchantability,
 * fitness for a particular purpose and non-infringement.
 */

package com.bitsofproof.dropwizard.shiro;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

/**
 * Things that can be configured via this bundle.
 * <p>
 * To specify a non-default location for the Shiro .ini file set the 'shiroConfigLocations' Servlet contextParam.
 * In DropWizard that's in http configuration's {@code contextParameters} Map.
 * </p>
 */
public class ShiroConfiguration {

	/**
	 * Default URL pattern for the ShiroFilter.
	 */
	final static String DEFAULT_SECURED_URL_PATTERN = "/*";

	/**
	 * Default is {@code false}.
	 */
	@JsonProperty
	boolean enabled = false;

	// could support more than one pattern ...
	/**
	 * Default is {@link #DEFAULT_SECURED_URL_PATTERN}.
	 */
	@JsonProperty("secured_url_pattern")
	String securedUrlPattern = DEFAULT_SECURED_URL_PATTERN;

	/**
	 * Whether this bundle is enabled.
	 * @return value of the {@code enabled} field. This is {@code false} by default.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Currently supports a single URL-pattern.
	 * @return the ShiroFilter will be configured to intercept URLS matching the returned url pattern.
	 */
	public String getSecuredUrlPattern() {
		return securedUrlPattern;
	}

	/**
	 * Override Object's {@link Object#toString()} implementation.
	 * @return a String containing the class name and the name:value pairs of the instance's fields.
	 */
	@Override
	public String toString() {
		return toStringHelper().toString();
	}

	/**
	 * If wishing to add additional fields to the {@link #toString()} return values, subclasses may override this method and call super() and then add their own parts to the result before returing it.
	 * @return an {@link Objects.ToStringHelper} instance populated with the class name and the name:value pairs of the instance's fields
	 */
	protected Objects.ToStringHelper toStringHelper() {
		return Objects.toStringHelper(this.getClass().getSimpleName())
		              .add("enabled (enabled)", enabled)
		              .add("securedUrlPattern (secured_url_pattern)", securedUrlPattern)
				;
	}

}

