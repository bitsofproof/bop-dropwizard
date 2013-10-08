package com.bitsofproof.security.shiro.dao.jdbi;

import org.junit.Before;

/**
 *
 */
public class TestUserDaoWithoutEnabled extends TestUserDAO {

    @Before
    public void beforeTest() {
        ((DefaultJdbiUserSecurityDAO)harness.getUserSecurityDAO()).setEnabledColumnUsed(false);
        harness.getUserDAO().setEnabledColumnUsed(false);
    }
}
