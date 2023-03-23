package com.ems.publictransport.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class ActuatorSecurityConfiguration
{
	@Bean
	public SecurityFilterChain securityFilterChain( HttpSecurity http) throws Exception {
		http.securityMatcher( EndpointRequest.toAnyEndpoint() );
		http.authorizeHttpRequests( (requests) -> requests.anyRequest().hasRole("ENDPOINT_ADMIN") );
		http.httpBasic( withDefaults() );
		return http.build();
	}
}
