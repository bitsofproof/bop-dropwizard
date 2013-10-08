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

import com.bitsofproof.security.shiro.dao.UserSecurityDAO;
import liquibase.Liquibase;
import liquibase.database.jvm.HsqlConnection;
import liquibase.logging.LogFactory;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import org.apache.shiro.io.ResourceUtils;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

/**
 * Project: security
 * DefaultUserImpl: ezra
 * Date: 3/26/13
 */
public class DatabaseUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DatabaseUtils.class);

    protected static final String DB_PROPERTIES_FILE_PATH = "classpath:test.db.properties";

    protected static final String DB_DRIVER_CLASSNAME_KEY = "jdbc.driver.className";
    protected static final String DB_CONNECTION_URL_KEY = "jdbc.connection.url";
    protected static final String DB_CONNECTION_USERNAME_KEY = "jdbc.connection.username";
    protected static final String DB_CONNECTION_PASSWORD_KEY = "jdbc.connection.password";

    // DEFAULT value - used value can be overridden by setting the jdbc.driver.className in the test.db.properties file.
    private static final String JDBC_DRIVER_CLASSNAME = "org.hsqldb.jdbcDriver";
    // DEFAULT value - value can be overridden by setting the jdbc.connection.url in the test.db.properties file.
    private static final String JDBC_CONNECTION_STRING = "jdbc:hsqldb:mem:testdb;shutdown=false";
    // DEFAULT value - value can be overridden by setting the jdbc.connection.username in the test.db.properties file.
    private static final String DB_USER_NAME = "sa";
    private static final String DB_PASSWORD = "";

    private static final String CHANGE_LOG = "src/main/resources/liquibase/shiro/master.xml";
    private static final String TEST_DATA_CHANGE_LOG = "src/test/resources/liquibase/test_changeset_2-5.sql";

    protected Properties dbProperties = null;
    protected DBI dbi;
    protected DefaultJdbiUserDAO userDAO;
    protected DefaultJdbiUserSecurityDAO userSecurityDAO;

    private void loadProperties(String resourcePath) {
        InputStream propStream = null;
        try {
            propStream = ResourceUtils.getInputStreamForPath(resourcePath);
        } catch (IOException iox) {
            LOG.info("No properties file found at {}, using default values.", resourcePath);
        }

        if (propStream != null) {
            dbProperties = new Properties();
            try {
                dbProperties.load(propStream);
            } catch (IOException iox) {
                LOG.error("Error loading properties from: " + resourcePath);
                throw new RuntimeException(iox);
            }
        }
    }

    public String getJdbcDriverClassname()
    {
        return (dbProperties != null && dbProperties.containsKey(DB_DRIVER_CLASSNAME_KEY)) ?
                dbProperties.getProperty(DB_DRIVER_CLASSNAME_KEY) : JDBC_DRIVER_CLASSNAME;
    }

    public String getJdbcConnectionString()
    {
        return (dbProperties != null && dbProperties.containsKey(DB_CONNECTION_URL_KEY)) ?
                dbProperties.getProperty(DB_CONNECTION_URL_KEY) : JDBC_CONNECTION_STRING;
    }

    public String getDbUsername()
    {
        return (dbProperties != null && dbProperties.containsKey(DB_CONNECTION_USERNAME_KEY)) ?
                dbProperties.getProperty(DB_CONNECTION_USERNAME_KEY) : DB_USER_NAME;
    }

    public String getDbPassword()
    {
        return (dbProperties != null && dbProperties.containsKey(DB_CONNECTION_PASSWORD_KEY)) ?
                dbProperties.getProperty(DB_CONNECTION_PASSWORD_KEY) : DB_PASSWORD;
    }

    private void performDatabaseSetupOrClean(boolean setup) {
        try {
            ResourceAccessor resourceAccessor = new FileSystemResourceAccessor();
            Class.forName(getJdbcDriverClassname());

            Connection holdingConnection = DriverManager.getConnection(getJdbcConnectionString(), getDbUsername(), getDbPassword());
            HsqlConnection hsconn = new HsqlConnection(holdingConnection);
            LogFactory.getLogger().setLogLevel("warning");
            Liquibase liquibase = new Liquibase(CHANGE_LOG, resourceAccessor, hsconn);
            liquibase.dropAll();
            if (setup) {
                liquibase.update("test");

                liquibase = new Liquibase(TEST_DATA_CHANGE_LOG, resourceAccessor, hsconn);
                liquibase.update("test");
            }

            hsconn.close();
        } catch (Exception ex) {
            String msg = setup ? "Error during database initialization" : "Error during database clean-up";
            LOG.error(msg, ex);
            throw new RuntimeException(msg, ex);
        }
    }

    public void setUp() {
        loadProperties(DB_PROPERTIES_FILE_PATH);

        performDatabaseSetupOrClean(true);

        dbi = new DBI(getJdbcConnectionString(), getDbUsername(), getDbPassword());
        userDAO = dbi.onDemand(DefaultJdbiUserDAO.class);
        userSecurityDAO = dbi.onDemand(DefaultJdbiUserSecurityDAO.class);
    }

    /**
     * Play nice with other test classes
     */
    public void tearDown()
    {
        if (dbProperties == null)
        {
            loadProperties(DB_PROPERTIES_FILE_PATH);
        }
        performDatabaseSetupOrClean(false);
        dbi.close(userSecurityDAO);
        dbi.close(userDAO);
    }

    public DBI getDbi() {
        return dbi;
    }

    public DefaultJdbiUserDAO getUserDAO() {
        return userDAO;
    }

    public UserSecurityDAO getUserSecurityDAO() {
        return userSecurityDAO;
    }
}
