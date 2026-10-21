package com.itam.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Bean Validation mesajlarını Spring MessageSource'a bağlar.
 * <p>
 * Bu yapılmazsa mesajlar ValidationMessages.properties dosyasından okunur ve
 * messages.properties içindeki Türkçe metinler kullanılamaz.
 */
@Configuration
public class ValidationConfig {

	@Bean
	public LocalValidatorFactoryBean validator(MessageSource messageSource) {
		LocalValidatorFactoryBean factory = new LocalValidatorFactoryBean();
		factory.setValidationMessageSource(messageSource);
		return factory;
	}
}
