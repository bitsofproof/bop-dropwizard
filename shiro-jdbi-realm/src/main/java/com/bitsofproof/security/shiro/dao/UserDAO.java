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

package com.bitsofproof.security.shiro.dao;

import com.bitsofproof.security.shiro.dao.jdbi.DefaultUserImpl;

import java.util.Iterator;

/**
 * Data Access Object for DefaultUserImpl instances.
 * <p/>
 * The UserDAO supports create/update/delete operations for DefaultUserImpl objects and association of same with DefaultRoleImpl objects. Note that it
 * does not, however, support management of the DefaultRoleImpl objects themselves nor the associated Permissions.
 */
public interface UserDAO extends UserSecurityDAO
{

	/**
	 * Get by primary key value.
	 */
	DefaultUserImpl getUser (Long userId);

	/**
	 * Lookup by username.  Return the associated Roles if the withRoles argument is true; otherwise just fetch the non-Collection DefaultUserImpl
	 * fields: id and password.
	 */
	DefaultUserImpl findUser (String username, boolean withRoles);

	Iterator<String> findAllUsernames ();

	/**
	 * Persist the user in the backing data store.  Create associations with the indicated Roles. DefaultUserImpl's userId will be generated and
	 * assigned if not provided.
	 *
	 * @param user The DefaultUserImpl object to persist.  Its userId is normally null when calling this method.
	 * @return the newly persisted DefaultUserImpl object's id which is also set on the object's id field.
	 */
	Long createUser (DefaultUserImpl user);

	/**
	 * Delete associated DefaultUserImpl object and any associations to its Roles from the backing store.
	 *
	 * @param userId the user to delete
	 */
	void deleteUser (Long userId);

	/**
	 * Update the DefaultUserImpl record in the backing store.  Operation may be conditional, occurring only if the value of one or more persisted
	 * fields has changed.  The persistent association to Roles is also updated to reflect the current state of the DefaultUserImpl provided as a
	 * parameter.
	 */
	void updateUser (DefaultUserImpl user);
}