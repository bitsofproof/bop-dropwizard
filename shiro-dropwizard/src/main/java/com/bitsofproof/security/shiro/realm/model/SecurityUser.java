package com.bitsofproof.security.shiro.realm.model;

/**
 * Represents a User for Shiro security purposes.
 */
public interface SecurityUser
{
	/**
	 * The one imposition we make is to assume that there's a numeric Id field on the user. In usage this is optional.
	 *
	 * @return the numeric id value for this User.
	 */
	Long getId ();

	/**
	 * Username is a string value
	 *
	 * @return the User's username value
	 */
	String getUsername ();

	/**
	 * Password are accessed via their string representation.
	 *
	 * @return the User's password value
	 */
	String getPassword ();

	/**
	 * The secret used to generate TOTP tokens.
	 *
	 * @return the User's totp secret
	 */
	String getTotpSecret ();
}
