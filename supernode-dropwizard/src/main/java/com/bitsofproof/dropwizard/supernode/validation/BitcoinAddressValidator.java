package com.bitsofproof.dropwizard.supernode.validation;

import com.bitsofproof.supernode.common.ValidationException;
import com.bitsofproof.supernode.wallet.AddressConverter;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

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
		if (value == null)
			return true;

		try
		{
			AddressConverter.fromSatoshiStyle ( value, addressFlag );
			return true;
		}
		catch (ValidationException e)
		{
			return false;
		}
	}
}
