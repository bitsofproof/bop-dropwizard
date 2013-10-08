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

import com.bitsofproof.security.shiro.dao.IdentifiedUserSecurityDAO;
import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import com.bitsofproof.security.shiro.realm.model.SecurityUser;
import com.google.common.base.Strings;
import org.apache.shiro.authc.AuthenticationException;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * A DAO implementation that makes assumptions about table and column names backing the User and Role entities. <p> Supports disabling of the primary
 * entities - User and Role - via a Boolean column named 'enabled'. If you schema does not have such a column in the User and Role entities set call
 * {@link #setEnabledColumnUsed(boolean)}, passing in {@code false}. </p>
 */
public abstract class DefaultJdbiUserSecurityDAO implements IdentifiedUserSecurityDAO
{

	protected final static String RolesPermissionsBaseSelectPrefix =
			"SELECT roles.role_name AS roleName, roles_permissions.permission AS permission"
					+ " FROM users_roles left join roles on users_roles.role_name = roles.role_name"
					+ " left join roles_permissions on roles.role_name = roles_permissions.role_name"
					+ " WHERE ";

	protected final static String EnabledRolesPermissionsBaseSelectPrefix =
			RolesPermissionsBaseSelectPrefix + "roles.enabled AND ";

	protected final static String UserRolesPermissionsBaseSelectPrefix =
			"SELECT users.user_Id AS userId, users.username AS username, users.password AS password,"
					+ " users.totp_secret AS totpSecret,"
					+ " roles.role_name AS roleName, roles_permissions.permission AS permission"
					+ " FROM users left join users_roles on users.user_id = users_roles.user_id"
					+ " left join roles on users_roles.role_name = roles.role_name"
					+ " left join roles_permissions on roles.role_name = roles_permissions.role_name"
					+ " WHERE ";

	protected final static String EnabledUserRolesPermissionsBaseSelectPrefix =
			UserRolesPermissionsBaseSelectPrefix + "users.enabled AND (roles.enabled OR roles.enabled IS NULL) AND ";

	private final static Logger LOG = LoggerFactory.getLogger ( DefaultJdbiUserSecurityDAO.class );

	protected boolean enabledColumnUsed = true;

	// Might be useful to make the User and Role classes pluggable. Define an iface with the setter methods and use reflection to create a new instance.

	/**
	 * Helper method for JDBI SQL Object. Builds a single DefaultUserImpl with associated DefaultRoleImpl & Permission sub-graph from tuples each of
	 * which was fetched into a UserRolePermissionJoinRow instance.
	 *
	 * @param i an iterator of the collection of UserRolePermissionJoinRow instances.
	 * @return a new DefaultUserImpl instance with fields and Roles/Permissions set.
	 */
	protected DefaultUserImpl extractObjectGraphFromJoinResults (Iterator<UserRolePermissionJoinRow> i)
	{
		DefaultUserImpl u = null;
		Map<String, SecurityRole> roles = new HashMap<> ();
		while (i.hasNext ())
		{
			UserRolePermissionJoinRow row = i.next ();
			if (u == null)
			{
				u = new DefaultUserImpl ();
				u.setId ( row.getUserId () );
				u.setUsername ( row.getUsername () );
				u.setPassword ( row.getPassword () );
			}
			// Could check that the user_id and username (etc.) are the same on all results.

			String roleName = row.getRoleName ();
			String permission = row.getPermission ();
			if (roleName != null)
			{
				if (!roles.containsKey ( roleName ))
				{
					roles.put ( roleName, new DefaultRoleImpl ( roleName ) );
				}
				if (permission != null)
				{
					roles.get ( roleName ).getPermissions ().add ( permission );
				}
				else
				{
					LOG.warn ( "Record found with null permission for role '{}'.", roleName );
				}
			}
			else if (permission != null)
			{
				LOG.warn ( "RoleName is null, but has a permission value of '{}'. How can that be?", permission );
			}
		}
		if (u != null)
		{
			u.setRoles ( new HashSet<> ( roles.values () ) );
		}

		return u;
	}

	@SqlQuery(EnabledUserRolesPermissionsBaseSelectPrefix + "users.username = :username")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<UserRolePermissionJoinRow> findEnabledUsersWithRolesAndPermissions (@Bind("username") String username);

	@SqlQuery(UserRolesPermissionsBaseSelectPrefix + "users.username = :username")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<UserRolePermissionJoinRow> findUsersWithRolesAndPermissions (@Bind("username") String username);

	public DefaultUserImpl findUser (String username)
	{
		checkArgument ( !Strings.isNullOrEmpty ( username ), "findUser() requires a non-null, non-empty username parameter." );
		Iterator<UserRolePermissionJoinRow> baseResults = enabledColumnUsed
				? findEnabledUsersWithRolesAndPermissions ( username )
				: findUsersWithRolesAndPermissions ( username );
		return extractObjectGraphFromJoinResults ( baseResults );
	}

	@SqlQuery("SELECT user_Id AS id, username, password, totp_secret AS totpSecret FROM users WHERE enabled AND username = :username")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<DefaultUserImpl> findEnabledUsersWithoutRoles (@Bind("username") String username);

	@SqlQuery("SELECT user_Id AS id, username, password, totp_secret AS totpSecret FROM users WHERE username = :username")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<DefaultUserImpl> findUsersWithoutRoles (@Bind("username") String username);

	public SecurityUser findUserWithoutRoles (String username)
	{
		checkArgument ( !Strings.isNullOrEmpty ( username ),
		                "findUserWithoutRoles() requires a non-null, non-empty username parameter." );
		DefaultUserImpl u = null;
		Iterator<DefaultUserImpl> users = isEnabledColumnUsed ()
				? findEnabledUsersWithoutRoles ( username )
				: findUsersWithoutRoles ( username );
		while (users != null && users.hasNext ())
		{
			if (u != null)
			{
				throw new AuthenticationException (
						"Username must be unique in the backing store. Multiple users found for username " + username );
			}
			u = users.next ();
		}
		return u;
	}

	@SqlQuery(EnabledRolesPermissionsBaseSelectPrefix + "users_roles.user_id = :userId")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<UserRolePermissionJoinRow> getEnabledUserRolesAndPermissions (@Bind("userId") Long userId);

	@SqlQuery(RolesPermissionsBaseSelectPrefix + "users_roles.user_id = :userId")
	@MapResultAsBean
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	protected abstract Iterator<UserRolePermissionJoinRow> getUserRolesAndPermissions (@Bind("userId") Long userId);

	/**
	 * Fetches just the Roles associated with the corresponding DefaultUserImpl.
	 *
	 * @param userId the id of the user
	 * @return the DefaultUserImpl's set of Roles or an empty Set.
	 */
	public Set<SecurityRole> getUserRoles (Long userId)
	{
		checkArgument ( userId != null, "getUserRoles() requires a non-null userId parameter." );
		Iterator<UserRolePermissionJoinRow> baseResults = isEnabledColumnUsed ()
				? getEnabledUserRolesAndPermissions ( userId )
				: getUserRolesAndPermissions ( userId );
		DefaultUserImpl u = extractObjectGraphFromJoinResults ( baseResults );
		return u != null ? u.getRoles () : Collections.<SecurityRole>emptySet ();
	}

	public Set<SecurityRole> getUserRoles (String username)
	{
		DefaultUserImpl u = findUser ( username ); // We'd need to do a 4-way join anyway, so just call findUser()
		return u != null ? u.getRoles () : Collections.<SecurityRole>emptySet ();
	}

	public boolean isEnabledColumnUsed ()
	{
		return enabledColumnUsed;
	}

	public void setEnabledColumnUsed (boolean enabledColumnUsed)
	{
		this.enabledColumnUsed = enabledColumnUsed;
	}
}