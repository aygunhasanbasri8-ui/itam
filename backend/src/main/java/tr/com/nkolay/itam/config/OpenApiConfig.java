package tr.com.nkolay.itam.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI itamOpenApi() {
		return new OpenAPI().info(new Info()
				.title("ITAM API")
				.version("v1")
				.description("Kurumsal IT Asset Management Sistemi REST API")
				.contact(new Contact().name("N Kolay - Bilgi Teknolojileri")));
	}
}
