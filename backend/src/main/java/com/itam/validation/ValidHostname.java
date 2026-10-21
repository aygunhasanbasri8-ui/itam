package com.itam.validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** RFC 1123 hostname biçim denetimi. null ve boş değer geçerli sayılır. */
@Documented
@Constraint(validatedBy = HostnameValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidHostname {

	String message() default "{itam.validation.hostname}";

	Class<?>[] groups() default {};

	Class<? extends Payload>[] payload() default {};
}
