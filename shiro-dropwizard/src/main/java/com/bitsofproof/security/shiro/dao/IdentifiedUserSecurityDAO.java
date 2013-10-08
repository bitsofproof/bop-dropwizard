package com.bitsofproof.security.shiro.dao;

import com.bitsofproof.security.shiro.realm.model.SecurityRole;

import java.util.Set;

/**
 * An extention of the UserSecurityDAO that provides support for storing a numeric SecurityUser identifier in the
 * Shiro PrincipalCollection.
 */
public interface IdentifiedUserSecurityDAO extends UserSecurityDAO {
	/**
	 * Used to retrieve the Roles associated with an {@link com.bitsofproof.security.shiro.realm.model.SecurityUser} when the principal stored in the session is
	 * a numeric {@link com.bitsofproof.security.shiro.realm.model.SecurityUser} identifier, such as the value returned from
	 * {@link com.bitsofproof.security.shiro.realm.model.SecurityUser#getId()}.
	 * <p>
	 * If using a {@link com.bitsofproof.security.shiro.realm.JdbiShiroRealm} and the first of the
	 * {@link com.bitsofproof.security.shiro.realm.JdbiShiroRealm#getPrincipalValueFields()} is the
	 * {@link com.bitsofproof.security.shiro.realm.JdbiShiroRealm#USER_ID}, then the DAO implementation
	 * used by the {@code JdbiShiroRealm} must implement this interface in order to handle
	 * authorization requests.
	 * </p>
	 *
	 * @param userId a numeric SecurityUser identifier
	 * @return the Set of Roles associated with the corresponding SecurityUser
	 */
	Set<SecurityRole> getUserRoles(Long userId);
}

