package com.bitsofproof.dropwizard.supernode.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = ExtendedKeyValidator.class)
public @interface ValidExtendedKey
{
	enum KeyType {
		PRIVATE, PUBLIC, ANY
	}

	KeyType keyType() default KeyType.ANY;

	String message () default "{com.bitsofproof.dropwizard.supernode.validation.ValidExtendedKey.message}";

	Class<?>[] groups () default {};

	Class<? extends Payload>[] payload () default {};

}
