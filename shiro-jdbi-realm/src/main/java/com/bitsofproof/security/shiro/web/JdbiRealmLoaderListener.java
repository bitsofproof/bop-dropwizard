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

package com.bitsofproof.security.shiro.web;

import com.bitsofproof.security.shiro.realm.JdbiShiroRealm;
import org.apache.shiro.mgt.RealmSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.env.WebEnvironment;
import org.apache.shiro.web.util.WebUtils;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This needs to be done a) after the Shiro EnvironmentLoader has run, and b) when we have the ServletContext.
 * Hence we can't do this directly in the service's run() method as the servlet is still being configured at that point.
 * This ServletContextListener meets our needs and lets the configuration occur.
 * <p>
 * This implementation assumes that there is a Shiro {@link RealmSecurityManager} instance in use.
 * </p>
 * <p>
 * If you use this class you must provide the shiro-web and the javax.servlet packages.
 * </p>
 * <p/>
 * Project: jdbi-realm
 * DefaultUserImpl: ezra
 * Date: 4/6/13
 */
public class JdbiRealmLoaderListener implements ServletContextListener {

	/**
	 * Used to control which Realm instance/s is/are initialized.
	 */
	public static enum RealmSelector {
		/**
		 * Initialize all instances of JdbiShiroRealm configured in the current SecurityManager.
		 */
		ALL,

		/**
		 * Only initialize the first JdbiShiroRealm encountered.
		 */
		FIRST
        /* , NAMED */ // Named might be a good option. OR could be based on 'name of variable' in shiro.ini config...
	}

	private static final Logger LOG = LoggerFactory.getLogger(JdbiRealmLoaderListener.class);

	private final DBI jdbi;
	private final RealmSelector whichRealm;

	/**
	 * Constructs an instance with the provided {@link DBI} instance, and {@link RealmSelector#ALL}.
	 *
	 * @param jdbi a DBI instance
	 */
	public JdbiRealmLoaderListener(DBI jdbi) {
		this(jdbi, RealmSelector.ALL);
	}

	public DBI getDbi() {
		return jdbi;
	}

	/**
	 * Constructs an instance with the provided {@link DBI} instance, and provided {@link RealmSelector}.
	 *
	 * @param jdbi a DBI instance, cannot be null
	 * @param whichRealm a RealmSelector value, defaults to {@link RealmSelector#ALL}
	 */
	public JdbiRealmLoaderListener(DBI jdbi, RealmSelector whichRealm) {
		checkArgument(jdbi != null, "jdbi is a required argument");
		this.jdbi = jdbi;
		if (whichRealm == null) {
			whichRealm = RealmSelector.ALL;
			LOG.info("no RealmSelector specified, defaulting to {}", whichRealm);
		}
		this.whichRealm = whichRealm;
	}

	/**
	 * Gets the RealmSecurityManager from the Shiro WebEnvironment. The configured Shiro SecurityManager must be an
	 * instance of {@link RealmSecurityManager}.
	 *
	 * @param sce used to get the ServletContext and from it the WebEnvironment.
	 * @return the Shiro {@code SecurityManager} cast to {@link RealmSecurityManager}
	 */
	protected RealmSecurityManager getRealmSecurityManager(ServletContextEvent sce) {
		WebEnvironment we = WebUtils.getWebEnvironment(sce.getServletContext());
		return (RealmSecurityManager) we.getSecurityManager();
	}


	/**
	 * Subclasses should override this method to change how Realm/s are initialized.
	 * Subclass implementations should ordinarily call this super impl.
	 * <p>
	 * This implementation calls {@link JdbiShiroRealm#setDbi(org.skife.jdbi.v2.DBI)},
	 * passing in the {@code DBI} instance from our own {@link #getDbi()} method.
	 * </p>
	 *
	 * @param realm the JdbiShiroRealm or subclass thereof being initialized
	 */
	protected void initializeRealm(JdbiShiroRealm realm) {
		LOG.debug("initializing JdbiShiroRealm '{}' with DBI instance", realm.getName());
		realm.setDbi(getDbi());
	}

	/**
	 * @param sce used to get the SecurityManager that has the Realms
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		RealmSecurityManager rsm = getRealmSecurityManager(sce);
		for (Realm r : rsm.getRealms()) {
			if (r instanceof JdbiShiroRealm) {
				initializeRealm((JdbiShiroRealm) r);
				if (whichRealm == RealmSelector.FIRST) {
					break;
				}
			}
		}

	}

	/**
	 * Subclasses should override this method to change how Realm/s are tidied up after use.
	 * Subclass implementations should ordinarily call this super impl.
	 * <p>
	 * This implementation calls {@link JdbiShiroRealm#close(org.skife.jdbi.v2.DBI)},
	 * passing in the {@code DBI} instance from our own {@link #getDbi()} method.

	 * </p>
	 *
	 * @param realm the JdbiShiroRealm or subclass thereof that will no longer be used.
	 */
	protected void destroyRealm(JdbiShiroRealm realm) {
		LOG.debug("closing JdbiShiroRealm's DBI-based DAO instance/s.", realm.getName());
		realm.close(getDbi());
	}

	/**
	 * When the app shuts down we close the UserDAOs on the JdbiShiroRealm instances we initialized.
	 *
	 * @param sce used to get the SecurityManager that has the Realms
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		RealmSecurityManager rsm = getRealmSecurityManager(sce);
		for (Realm r : rsm.getRealms())
			if (r instanceof JdbiShiroRealm) {
				destroyRealm((JdbiShiroRealm) r);
				if (whichRealm == RealmSelector.FIRST) {
					break;
				}
			}
	}
}