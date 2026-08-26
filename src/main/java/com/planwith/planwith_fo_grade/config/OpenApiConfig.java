package com.planwith.planwith_fo_grade.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

	private static final String BEARER_AUTH_SCHEME = "bearerAuth";

	@Value("${app.gateway.public-url:/}")
	private String gatewayPublicUrl;

	@Bean
	public OpenAPI planwithFoGradeOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-grade API")
						.description("""
								Call APIs through the API Gateway (:8000).
								Do not put Docker hostname or :8083 in OpenAPI servers.
								Swagger Try-it-out must use the browser origin (Gateway).
								Use Authorize to send HTTP Bearer JWT.
								""")
						.version("v1"))
				.servers(List.of(new Server()
						.url(gatewayPublicUrl)
						.description("API Gateway")))
				.components(new Components()
						.addSecuritySchemes(BEARER_AUTH_SCHEME, new SecurityScheme()
								.name(BEARER_AUTH_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.in(SecurityScheme.In.HEADER)))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH_SCHEME));
	}
}
