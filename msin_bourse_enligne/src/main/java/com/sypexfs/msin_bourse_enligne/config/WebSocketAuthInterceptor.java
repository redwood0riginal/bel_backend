package com.sypexfs.msin_bourse_enligne.config;

import com.sypexfs.msin_bourse_enligne.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

 // WebSocket handshake interceptor for JWT authentication
 // Validates JWT token from query parameter before establishing WebSocket connection

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements HandshakeInterceptor {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) throws Exception {
        try {
            // Extract token from query parameter
            String token = extractTokenFromQuery(request);
            
            if (token == null) {
                log.warn("WebSocket connection attempt without token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Validate token
            if (!jwtService.validateToken(token)) {
                log.warn("WebSocket connection attempt with invalid token");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return false;
            }

            // Extract user details and set authentication
            String username = jwtService.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
            
            // Store authentication in attributes for use in WebSocket session
            attributes.put("authentication", authentication);
            attributes.put("username", username);
            
            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            log.info("WebSocket connection authenticated for user: {}", username);
            return true;
            
        } catch (Exception e) {
            log.error("Error during WebSocket authentication: {}", e.getMessage());
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {}

    // Extract JWT token from query parameter

    private String extractTokenFromQuery(ServerHttpRequest request) {
        String query = request.getURI().getQuery();
        
        if (!StringUtils.hasText(query)) {
            return null;
        }
        
        // Parse query parameters
        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && "token".equals(keyValue[0])) {
                return keyValue[1];
            }
        }
        
        return null;
    }
}
