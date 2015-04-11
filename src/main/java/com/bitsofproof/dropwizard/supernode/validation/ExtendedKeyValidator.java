package com.bitsofproof.dropwizard.supernode.validation;

import com.bitsofproof.supernode.common.ExtendedKey;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static com.bitsofproof.dropwizard.supernode.validation.ValidExtendedKey.KeyType.*;

/**
 *
 */
public class ExtendedKeyValidator implements ConstraintValidator<ValidExtendedKey, ExtendedKey>
{
	private ValidExtendedKey.KeyType keyType;

	@Override
	public void initialize(ValidExtendedKey constraintAnnotation)
	{
		this.keyType = constraintAnnotation.keyType();
	}

	@Override
	public boolean isValid(ExtendedKey value, ConstraintValidatorContext context)
	{
		return keyType == ANY ||
				value == null ||
				!(keyType == PRIVATE && value.isReadOnly()) && !(keyType == PUBLIC && !value.isReadOnly());
	}
}
