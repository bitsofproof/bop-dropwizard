package com.bitsofproof.dropwizard.supernode.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = BitcoinAddressValidator.class)
public @interface BitcoinAddress
{
	int addressFlag() default 0x00;

	String message () default "Address must be a valid serialization of a bitcoin address";

	Class<?>[] groups () default {};

	Class<? extends Payload>[] payload () default {};
}
