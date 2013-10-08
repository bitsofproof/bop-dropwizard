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
import com.bitsofproof.security.shiro.realm.model.SecurityUser;
import com.google.common.base.Objects;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class represents a SecurityUser of the system and the user's set of SecurityRole objects. In the context of Shiro, the user is mapped to a
 * "subject."
 *
 * @see org.apache.shiro.subject.Subject
 */
public class DefaultUserImpl implements SecurityUser
{

	private Long id;

	private String username;

	private String password;

	private String totpSecret;

	private Set<SecurityRole> roles = new HashSet<> ();

	public DefaultUserImpl ()
	{
	}

	public DefaultUserImpl (Long id, String username, String password, String totpSecret, Set<SecurityRole> roles)
	{
		this.id = id;
		this.username = username;
		this.password = password;
		this.totpSecret = totpSecret;
		this.roles = (roles != null ? new HashSet<> ( roles ) : new HashSet<SecurityRole> ());
	}

	@Override
	public Long getId ()
	{
		return id;
	}

	/**
	 * Sets the DefaultUserImpl's id. The id is a required field. Normally this is assigned by the backing database.
	 *
	 * @param id DefaultUserImpl's assigned primary key identifier
	 */
	public void setId (Long id)
	{
		this.id = id;
	}

	/**
	 * Returns the username associated with this user account.
	 *
	 * @return the username associated with this user account.
	 */
	@Override
	public String getUsername ()
	{
		return username;
	}

	/**
	 * Sets this user account's username. The username must be set before the record is persisted.
	 *
	 * @param username unique and not-null
	 */
	public void setUsername (String username)
	{
		this.username = username;
	}

	/**
	 * Returns the hashed password for this user. <p> If the password is salted (and it should be) the salt is stored in the password. See, for example
	 * {@link org.apache.shiro.crypto.hash.format.ParsableHashFormat} and, in particular, {@link org.apache.shiro.crypto.hash.format.Shiro1CryptFormat}.
	 * </p>
	 *
	 * @return this user's password
	 */
	@Override
	public String getPassword ()
	{
		return password;
	}

	public void setPassword (String password)
	{
		this.password = password;
	}

	@Override
	public String getTotpSecret ()
	{
		return totpSecret;
	}

	public void setTotpSecret (String totpSecret)
	{
		this.totpSecret = totpSecret;
	}

	/**
	 * A user of the system has an associated set of ISecurityRole instances.
	 *
	 * @return the associated Set of ISecurityRole instances
	 */
	public Set<SecurityRole> getRoles ()
	{
		return roles;
	}

	public void setRoles (Set<SecurityRole> roles)
	{
		this.roles = roles;
	}

	/**
	 * Instance equality based on the value of the id and username fields. <p> Note that, as a consequence, unlike the default Java implementation, two
	 * instances of this class (or subclasses) are equal if their contents are equal. In particular this means that you cannot add more than one
	 * uninitialized instance to some collections (for example, to a Set). </p>
	 * <p/>
	 * Allow subclasses since some persistence frameworks wrap the POJOs behind the scenes. Allow nulls even for not-null columns so instances can be
	 * compared before they are persisted.
	 */
	@Override
	public boolean equals (Object o)
	{
		if (this == o) return true;
		if (!(o instanceof DefaultUserImpl)) return false;

		DefaultUserImpl user = (DefaultUserImpl) o;
		return Objects.equal ( id, user.id ) && Objects.equal ( username, user.username );
	}

	/**
	 * Follows the contract that link {@link #equals(Object)} and {@link #hashCode()}.
	 *
	 * @return hash code derived from id and username fields if they are not null
	 */
	@Override
	public int hashCode ()
	{
		int result = id != null ? id.hashCode () : 0;
		result = 31 * result + (username != null ? username.hashCode () : 0);
		return result;
	}

	/**
	 * Don't show value of password fields.
	 */
	@Override
	public String toString ()
	{
		return toStringHelper ().toString ();
	}

	protected Objects.ToStringHelper toStringHelper ()
	{
		return Objects.toStringHelper ( this.getClass ().getSimpleName () )
		              .add ( "id", id )
		              .add ( "username", username )
		              .add ( "password", "********" )
		              .add ( "totpSecret", "********" )
		              .add ( "roles", roles )
				;
	}
}