package com.bitsofproof.security.shiro.dao;


import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import com.bitsofproof.security.shiro.realm.model.SecurityUser;

import java.util.Set;

/**
 * A DAO API that exposes a minimal surface needed to support a Shiro AuthorizingRealm implementation.
 */
public interface UserSecurityDAO {

	/**
	 * Lookup by username. Usernames must be unique across {@link com.bitsofproof.security.shiro.realm.model.SecurityUser} records in the backing store.
	 */
	SecurityUser findUserWithoutRoles(String username);

	/**
	 * Used to retrieve the Roles associated with a {@link SecurityUser} when the principal stored in the session is a
	 * string {@link SecurityUser} identifier, such as the username used for authentication during login. The value
	 * itself is taken from the {@link SecurityUser#getUsername()} method.
	 * <p>
	 * If using a DAO implementation that only supports this interface and not the
	 * </p>
	 *
	 * @param username a string {@link SecurityUser} identifier - typically the username used when the
	 * {@link SecurityUser} is authenticated
	 * @return the Set of Roles associated with the corresponding ISecurityUser
	 */
	Set<SecurityRole> getUserRoles(String username);

}