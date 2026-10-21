package com.itam.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class IpAddressValidator implements ConstraintValidator<ValidIpAddress, String> {

	private static final Pattern IPV4 = Pattern.compile(
			"^(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)"
					+ "(\\.(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)){3}$");

	/** Tam yazım, :: kısaltması ve IPv4 gömülü karma yazım. */
	private static final Pattern IPV6 = Pattern.compile(
			"^("
					+ "([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}|"
					+ "([0-9a-fA-F]{1,4}:){1,7}:|"
					+ ":((:[0-9a-fA-F]{1,4}){1,7}|:)|"
					+ "([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}|"
					+ "([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}|"
					+ "([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}|"
					+ "([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}|"
					+ "([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}|"
					+ "[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})|"
					+ "::(ffff(:0{1,4})?:)?((25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)\\.){3}"
					+ "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]?\\d)"
					+ ")$");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		String trimmed = value.trim();
		return IPV4.matcher(trimmed).matches() || IPV6.matcher(trimmed).matches();
	}
}
