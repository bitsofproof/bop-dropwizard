package com.bitsofproof.security.shiro.dao.jdbi;

import com.bitsofproof.security.shiro.dao.UserDAO;
import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import com.google.common.collect.Sets;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.Transaction;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;
import org.skife.jdbi.v2.sqlobject.mixins.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Implementation of UserDAO via DBI.
 */
public abstract class DefaultJdbiUserDAO extends DefaultJdbiUserSecurityDAO implements UserDAO, Transactional<DefaultJdbiUserDAO>
{

	private final static Logger LOG = LoggerFactory.getLogger ( DefaultJdbiUserDAO.class );

	public DefaultUserImpl findUser (String username, boolean withRoles)
	{
		return withRoles ? super.findUser ( username ) : (DefaultUserImpl) super.findUserWithoutRoles ( username );
	}

	@SqlQuery(DefaultJdbiUserSecurityDAO.EnabledUserRolesPermissionsBaseSelectPrefix + "users.user_id = :userId")
	@MapResultAsBean
	protected abstract Iterator<UserRolePermissionJoinRow> getUserWithRolesAndPermissions (@Bind("userId") Long userId);

	@Override
	@Transaction(value = TransactionIsolationLevel.READ_COMMITTED)
	public DefaultUserImpl getUser (Long userId)
	{
		checkArgument ( userId != null, "getUser() requires a non-null userId parameter." );
		return extractObjectGraphFromJoinResults ( getUserWithRolesAndPermissions ( userId ) );
	}

	@Override
	@SqlQuery("select username from users ORDER BY username")
	public abstract Iterator<String> findAllUsernames ();

	@SqlUpdate("delete from users_roles where user_id = :userId")
	protected abstract void deleteUsersRoles (@Bind("userId") Long userId);

	@SqlUpdate("delete from users where user_id = :userId")
	protected abstract void deleteUserOnly (@Bind("userId") Long userId);

	@Override
	@Transaction
	public void deleteUser (Long userId)
	{
		checkArgument ( userId != null, "deleteUser() requires a non-null userId parameter." );
		deleteUsersRoles ( userId );
		deleteUserOnly ( userId );
	}

	@SqlUpdate("insert into users (username, password, totp_secret) values (:username, :password, :totpSecret)")
	protected abstract void createUserOnly (@Bind("username") String username,
	                                        @Bind("password") String password,
	                                        @Bind("totpSecret") String totpSecret);

	@SqlQuery("select user_id from users where username = :username")
	protected abstract Long fetchUserId (@Bind("username") String username);

	@SqlUpdate("insert into users_roles (user_id, role_name) values (:userId, :roleName)")
	protected abstract void createUserRole (@Bind("userId") Long userId, @Bind("roleName") String roleName);

	@Override
	@Transaction
	public Long createUser (DefaultUserImpl user)
	{
		checkNotNull ( user, "createUser(), user parameter cannot be null." );
		checkArgument ( user.getId () == null, "DefaultUserImpl's id field is assigned by the database and must be null." );
		Long userId;
		createUserOnly ( user.getUsername (), user.getPassword (), user.getTotpSecret () );
		userId = fetchUserId ( user.getUsername () );
		user.setId ( userId );
		for (SecurityRole r : user.getRoles ())
		{
			createUserRole ( userId, r.getName () );
		}

		return userId;
	}

	@SqlUpdate("update users set username=:username, password=:password, totp_secret=:totpSecret WHERE user_id=:userId")
	protected abstract void updateUserOnly (@Bind("userId") Long userId,
	                                        @Bind("username") String username,
	                                        @Bind("password") String password,
	                                        @Bind("totpSecret") String totpSecret);

	@SqlUpdate("delete from users_roles where user_id = :userId AND role_name = :roleName")
	protected abstract void deleteAUserRole (@Bind("userId") Long userId, @Bind("roleName") String roleName);

	/**
	 * Check if any of the persisted fields is different between the two DefaultUserImpl instances.
	 *
	 * @param u1 one DefaultUserImpl
	 * @param u2 another DefaultUserImpl
	 * @return true if username or password differs between u1 and u2; false if those fields are equal for the two DefaultUserImpl instances.
	 */
	boolean needToUpdateUser (DefaultUserImpl u1, DefaultUserImpl u2)
	{
		return !Objects.equals ( u1.getUsername (), u2.getUsername () )
				|| !Objects.equals ( u1.getPassword (), u2.getPassword () )
				|| !Objects.equals ( u1.getTotpSecret (), u2.getTotpSecret () )
				;
	}

	@Override
	@Transaction
	public void updateUser (DefaultUserImpl user)
	{
		checkNotNull ( user, "updateUser(), user parameter cannot be null." );
		Long userId = user.getId ();
		checkNotNull ( userId, "updateUser(): DefaultUserImpl's userId field must not be null." );

		// The getUser() would be overkill except we need it to delta the Roles.
		DefaultUserImpl oldU = getUser ( userId );
		checkNotNull ( oldU, "No user with id='%s' in the database (updateUser does not perform createOrUpdate, maybe it should).", userId );

		// Compute deltas.
		// Since we had to fetch the user to delta the Roles, we may as well delta the fields to skip the base record update if it's not needed.
		if (needToUpdateUser ( user, oldU ))
		{
			updateUserOnly ( user.getId (), user.getUsername (), user.getPassword (), user.getTotpSecret () );
		}
		Set<SecurityRole> rolesToRemove = Sets.difference ( oldU.getRoles (), user.getRoles () );
		if (rolesToRemove.size () > 0)
		{
			LOG.debug ( "Removing these Roles '{}' for user with id='{}'", rolesToRemove, userId );
		}
		for (SecurityRole r : rolesToRemove)
		{
			deleteAUserRole ( userId, r.getName () );
		}
		Set<SecurityRole> rolesToAdd = Sets.difference ( user.getRoles (), oldU.getRoles () );
		if (rolesToAdd.size () > 0)
		{
			LOG.debug ( "Adding these Roles '{}' for user with id='{}'", rolesToAdd, userId );
		}
		for (SecurityRole r : rolesToAdd)
		{
			createUserRole ( userId, r.getName () );
		}
	}

}
