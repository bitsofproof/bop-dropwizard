package com.bitsofproof.dropwizard.auth;

import java.lang.annotation.*;

@Documented
@Retention (RetentionPolicy.RUNTIME)
@Target ({ElementType.PARAMETER})
public @interface RestrictedTo
{
	boolean required() default true;

	boolean secondFactor () default false;
}
