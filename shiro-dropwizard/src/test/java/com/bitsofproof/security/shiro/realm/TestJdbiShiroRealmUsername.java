package com.bitsofproof.security.shiro.realm;

import com.bitsofproof.security.shiro.SecondFactorPrincipal;
import com.bitsofproof.security.shiro.dao.jdbi.DefaultRoleImpl;
import com.bitsofproof.security.shiro.dao.jdbi.DefaultUserImpl;
import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Project: security
 * DefaultUserImpl: ezra
 * Date: 4/3/13
 */
public class TestJdbiShiroRealmUsername extends TestJdbiShiroRealm {

    private static final Logger LOG = LoggerFactory.getLogger(TestJdbiShiroRealmUsername.class);

    protected Logger log()
    {
        return LOG;
    }

    protected String getUsername()
    {
        return "ShiroTest_uname";
    }

    protected String[] getExpectedPermissions() {
        return new String[]{"super", "foo", SecondFactorPrincipal.PERMISSION};
    }

    protected String getPlainTextPassword()
    {
        return "AClearPassword#456";
    }

    protected Set<SecurityRole> getRoles()
    {
        // Permissions aren't involved in user creation, just the role name
        return Collections.<SecurityRole>singleton(new DefaultRoleImpl ("admin"));
    }

    // Should be fine to have the username be the principal.
    protected void configureRealm(JdbiShiroRealm realm) {
        //realm.setPrincipalValueFields(Arrays.asList(JdbiShiroRealm.PrincipalValueField.USERNAME));
    }

    protected void checkStoredPrincipal(DefaultUserImpl u, Object p) {
        //assertEquals("CurrentUser is expected to store the user's username as the principal.", u.getUsername(), p);
    }

}
