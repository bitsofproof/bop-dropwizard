package com.bitsofproof.dropwizard.supernode.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.bitsofproof.supernode.api.Address;
import com.bitsofproof.supernode.common.ValidationException;

public class BitcoinAddressValidator implements ConstraintValidator<BitcoinAddress, String>
{
	private int addressFlag;

	@Override
	public void initialize (BitcoinAddress constraintAnnotation)
	{
		this.addressFlag = constraintAnnotation.addressFlag ();
	}

	@Override
	public boolean isValid (String value, ConstraintValidatorContext context)
	{
		if ( value == null )
		{
			return true;
		}

		try
		{
			Address.fromSatoshiStyle (value, addressFlag);
			return true;
		}
		catch ( ValidationException e )
		{
			return false;
		}
	}
}
