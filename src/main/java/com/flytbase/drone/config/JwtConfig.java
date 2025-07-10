package com.flytbase.drone.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Configuration properties for JWT. */
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
@Data
public class JwtConfig {

  /** Secret key used for signing JWT tokens. */
  private String secret;

  /** Expiration time in milliseconds. */
  private long expirationMs;
}
