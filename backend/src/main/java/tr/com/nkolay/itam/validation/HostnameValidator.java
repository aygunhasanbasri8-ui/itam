package tr.com.nkolay.itam.validation;

import java.util.regex.Pattern;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class HostnameValidator implements ConstraintValidator<ValidHostname, String> {

	private static final int MAX_LENGTH = 253;

	private static final Pattern LABEL =
			Pattern.compile("^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?$");

	@Override
	public boolean isValid(String value, ConstraintValidatorContext context) {
		if (value == null || value.isBlank()) {
			return true;
		}
		String host = value.trim();
		if (host.length() > MAX_LENGTH || host.endsWith(".")) {
			return false;
		}
		for (String label : host.split("\\.", -1)) {
			if (!LABEL.matcher(label).matches()) {
				return false;
			}
		}
		return true;
	}
}
