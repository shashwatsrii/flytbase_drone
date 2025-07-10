package com.flytbase.drone.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration for WebSocket communication. Enables STOMP messaging and configures endpoints for
 * real-time mission monitoring.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    // Enable a simple in-memory message broker to carry messages back to the client
    // on destinations prefixed with /topic
    config.enableSimpleBroker("/topic");

    // Set prefix for messages bound for @MessageMapping methods
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // Register the "/ws" endpoint, enabling SockJS fallback options for browsers that don't
    // support WebSocket natively
    registry
        .addEndpoint("/ws")
        .setAllowedOrigins("*") // Enable CORS for WebSocket
        .withSockJS();
  }
}
