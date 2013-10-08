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

package com.bitsofproof.security.shiro.dao.jdbi;

import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import com.google.common.base.Objects;

import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This class represents a DefaultRoleImpl that can be assigned to a {@link DefaultUserImpl}.
 */
public class DefaultRoleImpl implements SecurityRole
{
	private final String name;

	private final Set<String> permissions;

	public DefaultRoleImpl (String name)
	{
		this ( name, null );
	}

	public DefaultRoleImpl (String name, Set<String> permissions)
	{
		checkArgument ( name != null, "A DefaultRoleImpl's name cannot be null." );
		this.name = name;
		this.permissions = (permissions != null ? permissions : new HashSet<String> ());
	}

	@Override
	public String getName ()
	{
		return name;
	}

	@Override
	public Set<String> getPermissions ()
	{
		return permissions;
	}

	/**
	 * Instance equality based on the value of the name and permissions fields. <p> Note that, as a consequence, unlike the default Java implementation,
	 * two instances of this class (or subclasses) are equal if their contents are equal. In particular this means that you cannot add more than one
	 * uninitialized instance to some collections (for example, to a Set). </p> Allow subclasses since some persistence frameworks wrap the POJOs behind
	 * the scenes. Allow nulls even for not-null columns so instances can be compared before they are persisted.
	 */
	@Override
	public boolean equals (Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DefaultRoleImpl)) return false;

		DefaultRoleImpl role = (DefaultRoleImpl) o;
		return (name.equals ( role.name )) && (permissions.equals ( role.permissions ));
	}

	@Override
	public int hashCode ()
	{
		int result = name.hashCode ();
		result = 31 * result + permissions.hashCode ();
		return result;
	}

	@Override
	public String toString ()
	{
		return toStringHelper ().toString ();
	}

	protected Objects.ToStringHelper toStringHelper ()
	{
		return Objects.toStringHelper ( this.getClass ().getSimpleName () )
		              .add ( "name", name )
		              .add ( "permissions", permissions )
				;
	}

}

