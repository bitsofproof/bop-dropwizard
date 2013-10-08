package com.bitsofproof.security.shiro.realm;

import com.bitsofproof.security.shiro.SecondFactorPrincipal;
import com.bitsofproof.security.shiro.authc.TOTPUsernamePasswordToken;
import com.bitsofproof.security.shiro.authc.credential.TOTPUserPasswordMatcher;
import com.bitsofproof.security.shiro.dao.IdentifiedUserSecurityDAO;
import com.bitsofproof.security.shiro.dao.UserSecurityDAO;
import com.bitsofproof.security.shiro.dao.jdbi.DefaultJdbiUserSecurityDAO;
import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import com.bitsofproof.security.shiro.realm.model.SecurityUser;
import com.google.common.base.Strings;
import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.CredentialsMatcher;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * A Shiro security realm with SecurityUser (Subject) data provided by a DAO implemented via JDBI.
 */
public class JdbiShiroRealm extends AuthorizingRealm
{

	private static final Logger LOG = LoggerFactory.getLogger ( JdbiShiroRealm.class );

	protected UserSecurityDAO userSecurityDAO = null;

	protected boolean daoFromDbi = false;

	/**
	 * Creates a new instance with no UserDAO. Calls {@link #JdbiShiroRealm(UserSecurityDAO)} passing in null. <p/> <p>UserDAO would need to be set via
	 * {@link #setUserSecurityDAO(UserSecurityDAO)} before using the instance. </p>
	 */
	public JdbiShiroRealm ()
	{
		this ( (UserSecurityDAO) null );
	}

	/**
	 * Create a JdbiShiroRealm using the provided DBI instance.  An onDemand UserDAO will be created based on the {@link
	 * com.bitsofproof.security.shiro.dao.jdbi.DefaultJdbiUserSecurityDAO} class and used to call {@link #JdbiShiroRealm(UserSecurityDAO)}.
	 *
	 * @param dbi DBI instance to use to create a UserDAO.
	 */
	public JdbiShiroRealm (DBI dbi)
	{
		this ( dbi.onDemand ( DefaultJdbiUserSecurityDAO.class ) );
		daoFromDbi = true;
	}

	/**
	 * Creates an instance with the specified {@code UserSecurityDAO}. Calls {@link #JdbiShiroRealm(org.apache.shiro.authc.credential.CredentialsMatcher,
	 * UserSecurityDAO)} passing in a new {@link TOTPUserPasswordMatcher} with its default settings, and {@code userSecurityDAO}.
	 *
	 * @param userSecurityDAO a {@link UserSecurityDAO} used to retrieve user credentials and role/permission data.
	 */
	public JdbiShiroRealm (UserSecurityDAO userSecurityDAO)
	{
		this ( new TOTPUserPasswordMatcher (), userSecurityDAO );
	}

	/**
	 * Creates an instance with the specified {@link CredentialsMatcher} and {@link UserSecurityDAO}. Calls {@link
	 * AuthorizingRealm#AuthorizingRealm(org.apache.shiro.authc.credential.CredentialsMatcher)} passing in {@code matcher}.  If {@code userSecurityDAO}
	 * is not null, it is set via {@link #setUserSecurityDAO(UserSecurityDAO)}}.
	 *
	 * @param matcher         the {@link CredentialsMatcher} to use for authenticating users.
	 * @param userSecurityDAO the {@link UserSecurityDAO} to use for looking up {@link com.bitsofproof.security.shiro.dao.jdbi.DefaultUserImpl}s.
	 */
	public JdbiShiroRealm (CredentialsMatcher matcher, UserSecurityDAO userSecurityDAO)
	{
		super ( matcher );
		this.userSecurityDAO = userSecurityDAO;
		setAuthenticationTokenClass ( TOTPUsernamePasswordToken.class );
	}

	/**
	 * Sets the {@link #userSecurityDAO} to an {@link DBI#onDemand(Class)} instance of the {@link DefaultJdbiUserSecurityDAO} class.  If you wish to use
	 * a different {@link UserSecurityDAO} or {@link com.bitsofproof.security.shiro.dao.IdentifiedUserSecurityDAO} implementation then either call {@link
	 * #setUserSecurityDAO(UserSecurityDAO)} directly, or override this method.
	 *
	 * @param dbi a DBI instance to use to create the {@link com.bitsofproof.security.shiro.dao.IdentifiedUserSecurityDAO} from the {@link
	 *            DefaultJdbiUserSecurityDAO} implementation.
	 */
	public void setDbi (DBI dbi)
	{
		this.userSecurityDAO = (dbi == null) ? null : dbi.onDemand ( DefaultJdbiUserSecurityDAO.class );
		daoFromDbi = true;
	}

	/**
	 * Closes the {@link #userSecurityDAO} if it was created via the {@link #setDbi(org.skife.jdbi.v2.DBI)} method.
	 *
	 * @param dbi A {@link DBI} instance to use to close the {@link #userSecurityDAO}.  Ordinarily it should be the same {@link DBI} instance previously
	 *            passed in to {@link #setDbi(org.skife.jdbi.v2.DBI)}.
	 */
	public void close (DBI dbi)
	{
		if (dbi != null && userSecurityDAO != null && daoFromDbi)
		{
			dbi.close ( userSecurityDAO );
			this.userSecurityDAO = null;
			daoFromDbi = false;
		}
	}

	public UserSecurityDAO getUserSecurityDAO ()
	{
		return userSecurityDAO;
	}

	public void setUserSecurityDAO (UserSecurityDAO UserSecurityDAO)
	{
		this.userSecurityDAO = UserSecurityDAO;
		daoFromDbi = false;
	}

	public void afterPropertiesSet ()
	{
		checkState ( UsernamePasswordToken.class.isAssignableFrom ( getAuthenticationTokenClass () ),
		             "This JdbiShiroRealm is coded to work with UsernamePasswordToken instances." );
		if (userSecurityDAO == null)
		{
			throw new IllegalStateException ( "Configuration error: To function as a Realm instance, userSecurityDAO must not be null." );
		}
	}

	@Override
	protected AuthenticationInfo doGetAuthenticationInfo (AuthenticationToken authNToken) throws AuthenticationException
	{
		checkArgument ( UsernamePasswordToken.class.isAssignableFrom ( authNToken.getClass () ),
		                "doGetAuthenticationInfo(): AuthenticationToken argument needs to be an instance of UsernamePasswordToken. Was an instance of '%s' instead.",
		                authNToken.getClass ().getName () );
		UsernamePasswordToken upToken = (UsernamePasswordToken) authNToken;

		String username = upToken.getUsername ();
		if (Strings.isNullOrEmpty ( username ))
		{
			LOG.error ( "doGetAuthenticationInfo() requires a non-null, non-empty username" );
			throw new AccountException ( "username is required by this realm." );
		}

		SecurityUser user;
		try
		{
			// No need to fetch the Roles at this point.
			user = getUserSecurityDAO ().findUserWithoutRoles ( username );
		}
		catch (RuntimeException ex)
		{
			LOG.error ( "Error retrieving user '{}' from database. {}", username, ex.getMessage () );
			if (ex instanceof AuthenticationException)
			{
				throw ex;
			}
			else
			{
				throw new AuthenticationException ( "Error retrieving user '" + username + "'.", ex );
			}
		}
		if (user != null)
		{
			if (!username.equals ( user.getUsername () ))
			{
				LOG.error ( "Database is inconsistent.  Queried for user with username of '{}', retrieved username of '{}'.",
				            username, user.getUsername () );
				throw new AccountException ( "database error: username mis-match" );
			}
			String password = user.getPassword ();
			if (password == null)
			{
				LOG.warn ( "Password is required and username '{}' has a null password. Treating account as disabled.", username );
				throw new DisabledAccountException ( "No valid account found for user '" + username + "'." );
			}

			// setting up principals and credentials. based on the presence of totp_secret
			// About to use the PrincipalValues, set a flag.
			Set<Object> principalVals = new LinkedHashSet<> ( );
			principalVals.add ( user.getId () );
			principalVals.add ( user.getUsername () );

			// if user has a totpSecret, add it to the credentials
			Object credentials;
			if (user.getTotpSecret () == null)
			{
				credentials = password;
				// if the user has no totpSecret, we assume that they don't want to use second factor, effectively they always authenticated
				// as if the second factor auth used.
				principalVals.add ( new SecondFactorPrincipal () );
			}
			else
			{
				// If the user wants to use second factor authc and they provide a TOTP token, then we add SecondFactorPrincipal
				if (authNToken instanceof TOTPUsernamePasswordToken && ((TOTPUsernamePasswordToken)authNToken).getTotpToken () != null) {
					principalVals.add ( new SecondFactorPrincipal () );
				}
				credentials = new String[]{password, user.getTotpSecret ()};
			}

			SimplePrincipalCollection spc = new SimplePrincipalCollection ( principalVals, getName () );
			LOG.debug ( "Found user record. Returning authentication info with principal collection of: {}", spc );
			return new SimpleAuthenticationInfo ( spc, credentials );
		}
		else
		{
			return null;
		}
	}

	@Override
	protected AuthorizationInfo doGetAuthorizationInfo (PrincipalCollection principals)
	{
		if (principals == null)
		{
			LOG.error ( "PrincipalCollection argument (principals) should never be null. Returning null AuthorizationInfo." );
			return null;
		}
		Object principalId = getAvailablePrincipal ( principals );
		if (principalId == null)
		{
			LOG.error ( "No principal available; no one to authorize. Returning null AuthorizationInfo." );
			return null;
		}

		LOG.debug ( "Retrieving Roles & Permissions for Subject (aka, SecurityUser) identified by '{}'.", principalId );

		Set<SecurityRole> roles;
		try
		{
			if (principalId instanceof Long)
			{
				LOG.debug ( "Current principalId is of type Long, treating as a PrincipalValueField.USER_ID value." );
				UserSecurityDAO usd = getUserSecurityDAO ();
				if (usd instanceof IdentifiedUserSecurityDAO)
				{
					roles = ((IdentifiedUserSecurityDAO) getUserSecurityDAO ()).getUserRoles ( (Long) principalId );
				}
				else
				{
					throw new IllegalStateException (
							"UserSecurityDAO must be an instance of the IdentifiedUserSecurityDAO sub-type for this operation. PrincipalCollection's available principal is of type Long, the DAO needs to expose the getUserRoles((Long) principalId) method to support this usage, or change the principalValueFields to make USERNAME the primary principalId." );
				}
			}
			else if (principalId instanceof String)
			{
				LOG.debug ( "Current principalId is of type String, treating as a PrincipalValueField.USERNAME value." );
				roles = getUserSecurityDAO ().getUserRoles ( (String) principalId );
			}
			else
			{
				LOG.error ( "The provided principal is of an unsupported type. " +
						            "This method supports Long and String typed identifiers.  " +
						            "Provided type was {}; provided value was: {}.",
				            principalId.getClass ().getName (), principalId );
				throw new AuthorizationException (
						"The provided principal is of an unsupported type. This method supports Long and String typed identifiers.  Provided type was " + principalId
								.getClass ()
								.getName () + "; provided value was: " + principalId );
			}
		}
		catch (RuntimeException ex)
		{
			LOG.error ( "Error retrieving Roles from database for user with identifier '" + principalId + "'.", ex );
			throw new AuthorizationException ( "No account found for user identified by '" + principalId + "'.", ex );
		}
		SimpleAuthorizationInfo info = new SimpleAuthorizationInfo ();
		for (SecurityRole role : roles)
		{
			LOG.debug ( "User: '{}', adding role '{}'.", principalId, role );
			info.addRole ( role.getName () );
			info.addStringPermissions ( role.getPermissions () );
		}

		// last, if there is a SecondFactorPrincipal, we
		Collection<SecondFactorPrincipal> sfp = principals.byType ( SecondFactorPrincipal.class );
		if (!sfp.isEmpty ())
		{
			//info.addRole("TOTP");
			info.addStringPermission ( SecondFactorPrincipal.PERMISSION );
		}

		return info;
	}

}
