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

	public static final String BEARER_SCHEME = "Bearer";
	public static final String GATEWAY_USER_ID_SCHEME = "X-Auth-User-Id";

	@Value("${app.gateway.public-url:/}")
	private String gatewayPublicUrl;

	@Bean
	public OpenAPI planwithFoGradeOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("PlanWith planwith-fo-grade API")
						.description("""
								Grade API. Access Token 검증은 Gateway.
								브라우저·다른 PC는 Gateway `:8000`만 호출한다.
								Gateway Swagger에서 인증 API는 Authorize → **Bearer**에
								로그인 응답 `accessToken`을 넣는다.
								Gateway는 클라이언트가 보낸 `X-Auth-User-Id`를 제거한다.
								Grade를 `:8083`으로 직접 칠 때만 `X-Auth-User-Id`에 memberUuid를 넣는다.
								""")
						.version("v1"))
				.servers(List.of(new Server()
						.url(gatewayPublicUrl)
						.description("API Gateway")))
				.addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
				.addSecurityItem(new SecurityRequirement().addList(GATEWAY_USER_ID_SCHEME))
				.components(new Components()
						.addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")
								.description("Gateway `:8000` 호출용. 로그인 응답 accessToken"))
						.addSecuritySchemes(GATEWAY_USER_ID_SCHEME, new SecurityScheme()
								.name("X-Auth-User-Id")
								.type(SecurityScheme.Type.APIKEY)
								.in(SecurityScheme.In.HEADER)
								.description("Grade 직접 호출(`:8083`)용. Gateway 경유 시 이 헤더는 버려진다")));
	}
}
