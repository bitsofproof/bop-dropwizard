package com.bitsofproof.security.shiro.realm;

import com.bitsofproof.security.shiro.SecondFactorPrincipal;
import com.bitsofproof.security.shiro.dao.jdbi.DefaultRoleImpl;
import com.bitsofproof.security.shiro.realm.model.SecurityRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Project: security
 * DefaultUserImpl: ezra
 * Date: 4/3/13
 */
public class TestJdbiShiroRealmUsernameId extends TestJdbiShiroRealmUsername {

    private static final Logger LOG = LoggerFactory.getLogger(TestJdbiShiroRealmUsernameId.class);

    protected Logger log()
    {
        return LOG;
    }

    protected String getUsername()
    {
        return "ShiroTest_uname_id";
    }

    protected String[] getExpectedPermissions() {
        return new String[]{"super", "foo", "bar", SecondFactorPrincipal.PERMISSION};
    }

    protected Set<SecurityRole> getRoles()
    {
        // Permissions aren't involved in user creation, just the role name
        return new HashSet<SecurityRole>(asList(new DefaultRoleImpl ("admin"), new DefaultRoleImpl("user")));
    }

    // Should be fine to have the username be the principal.
    protected void configureRealm(JdbiShiroRealm realm) {
        //realm.setPrincipalValueFields(asList(JdbiShiroRealm.PrincipalValueField.USERNAME, JdbiShiroRealm.PrincipalValueField.USER_ID));
    }

}
