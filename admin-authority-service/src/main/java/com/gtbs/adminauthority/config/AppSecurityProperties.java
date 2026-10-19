package com.gtbs.adminauthority.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record AppSecurityProperties(boolean enabled) {}
