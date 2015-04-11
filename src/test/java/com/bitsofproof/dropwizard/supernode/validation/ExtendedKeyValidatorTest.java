package com.bitsofproof.dropwizard.supernode.validation;

import com.bitsofproof.supernode.common.ExtendedKey;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.validation.*;
import java.util.ArrayList;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

/**
 *
 */
public class ExtendedKeyValidatorTest
{
	@BeforeClass
	public static void setUp() throws Exception
	{
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}

	private static final class TestBean
	{
		@Valid
		@ValidExtendedKey
		ExtendedKey anyKey;

		@Valid
		@ValidExtendedKey(keyType = ValidExtendedKey.KeyType.PUBLIC)
		ExtendedKey pubKey;

		@Valid
		@ValidExtendedKey(keyType = ValidExtendedKey.KeyType.PRIVATE)
		ExtendedKey privKey;
	}
	private static Validator validator;

	@Test
	public void testValidCase()
	{
		TestBean b = new TestBean();
		b.anyKey = ExtendedKey.createNew();
		b.privKey = ExtendedKey.createNew();
		b.pubKey = ExtendedKey.createNew().getReadOnly();

		assertValid(b);

		b = new TestBean();
		b.anyKey = ExtendedKey.createNew().getReadOnly();
		b.privKey = ExtendedKey.createNew();
		b.pubKey = ExtendedKey.createNew().getReadOnly();

		assertValid(b);
	}

	@Test
	public void testInvalidPrivate()
	{
		TestBean b = new TestBean();
		b.anyKey = ExtendedKey.createNew().getReadOnly();
		b.privKey = ExtendedKey.createNew().getReadOnly();
		b.pubKey = ExtendedKey.createNew();

		Set<ConstraintViolation<TestBean>> violations = validator.validate(b);
		assertThat(violations).hasSize(2)
							  .extracting("message")
							  .contains("Extended key must be PRIVATE",
										"Extended key must be PUBLIC");
	}

	private void assertValid(TestBean bean)
	{
		Set<ConstraintViolation<TestBean>> violations = validator.validate(bean);
		assertEquals(0, violations.size());
	}
}
