package com.gtbs.adminauthority.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, AppSecurityProperties securityProperties) throws Exception {
        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.csrf(csrf -> csrf.disable());

        if (!securityProperties.enabled()) {
            http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        } else {
            http.authorizeHttpRequests(
                    auth -> auth.requestMatchers(
                                    "/actuator/**",
                                    "/api/docs/**",
                                    "/api/swagger-ui/**",
                                    "/api/swagger-ui.html",
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**")
                            .permitAll()
                            .requestMatchers("/authority/**")
                            .hasAuthority("ROLE_TRAFFIC_AUTHORITY")
                            .anyRequest()
                            .denyAll());
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));
        }
        return http.build();
    }

    private static Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        return jwt -> {
            Collection<GrantedAuthority> authorities = extractRoles(jwt);
            return new JwtAuthenticationToken(jwt, authorities);
        };
    }

    private static Collection<GrantedAuthority> extractRoles(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .collect(Collectors.toSet());
    }
}
