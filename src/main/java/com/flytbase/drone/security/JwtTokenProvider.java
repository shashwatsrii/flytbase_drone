package com.flytbase.drone.security;

import com.flytbase.drone.config.JwtConfig;
import io.jsonwebtoken.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/** Provider for JWT token generation and validation. */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {

  private final JwtConfig jwtConfig;

  /**
   * Generate a JWT token for the given authentication.
   *
   * @param authentication the authentication object
   * @return the generated JWT token
   */
  public String generateToken(Authentication authentication) {
    UserDetails userDetails = (UserDetails) authentication.getPrincipal();

    String authorities =
        authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));

    Date now = new Date();
    Date expiryDate = new Date(now.getTime() + jwtConfig.getExpirationMs());

    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .claim("roles", authorities)
        .setIssuedAt(now)
        .setExpiration(expiryDate)
        .signWith(SignatureAlgorithm.HS512, jwtConfig.getSecret())
        .compact();
  }

  /**
   * Get user email from JWT token.
   *
   * @param token the JWT token
   * @return the user email
   */
  public String getUserEmailFromToken(String token) {
    Claims claims =
        Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token).getBody();

    return claims.getSubject();
  }

  /**
   * Get authentication from JWT token.
   *
   * @param token the JWT token
   * @return the authentication object
   */
  public Authentication getAuthentication(String token) {
    Claims claims =
        Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token).getBody();

    Collection<? extends GrantedAuthority> authorities =
        Arrays.stream(claims.get("roles").toString().split(","))
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());

    UserDetails principal = new User(claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  /**
   * Validate JWT token.
   *
   * @param token the JWT token
   * @return true if the token is valid, false otherwise
   */
  public boolean validateToken(String token) {
    try {
      Jwts.parser().setSigningKey(jwtConfig.getSecret()).parseClaimsJws(token);
      return true;
    } catch (SignatureException ex) {
      log.error("Invalid JWT signature");
    } catch (MalformedJwtException ex) {
      log.error("Invalid JWT token");
    } catch (ExpiredJwtException ex) {
      log.error("Expired JWT token");
    } catch (UnsupportedJwtException ex) {
      log.error("Unsupported JWT token");
    } catch (IllegalArgumentException ex) {
      log.error("JWT claims string is empty");
    }
    return false;
  }

  /**
   * Resolve token from request.
   *
   * @param request the HTTP request
   * @return the JWT token
   */
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
