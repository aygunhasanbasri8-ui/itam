package tr.com.nkolay.itam.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * IPv4 veya IPv6 biçim denetimi.
 * null ve boş değer geçerli sayılır
 */
@Documented
@Constraint(validatedBy = IpAddressValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidIpAddress {

	String message() default "{itam.validation.ip}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
