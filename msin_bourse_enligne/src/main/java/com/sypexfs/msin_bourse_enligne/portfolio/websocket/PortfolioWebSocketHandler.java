package com.sypexfs.msin_bourse_enligne.portfolio.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sypexfs.msin_bourse_enligne.portfolio.dto.PortfolioDetailResponse;
import com.sypexfs.msin_bourse_enligne.portfolio.dto.PortfolioResponse;
import com.sypexfs.msin_bourse_enligne.portfolio.service.PortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;


@Component
@RequiredArgsConstructor
@Slf4j
public class PortfolioWebSocketHandler extends TextWebSocketHandler {

    private final PortfolioService portfolioService;
    private final ObjectMapper objectMapper;

    // Map of portfolioId -> Set of WebSocket sessions
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> portfolioSubscriptions = new ConcurrentHashMap<>();
    
    // Map of userId -> Set of WebSocket sessions
    private final Map<Long, CopyOnWriteArraySet<WebSocketSession>> userSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sendMessage(session, Map.of(
                "type", "connection",
                "status", "connected",
                "message", "Portfolio WebSocket connected"
        ));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message: {}", payload);

            @SuppressWarnings("unchecked")
            Map<String, Object> request = objectMapper.readValue(payload, Map.class);
            String action = (String) request.get("action");

            if (action == null) {
                log.warn("Received message without action from session: {}", session.getId());
                sendError(session, "Missing 'action' field");
                return;
            }

            switch (action) {
                case "subscribe_portfolio":
                    handleSubscribePortfolio(session, request);
                    break;
                case "subscribe_user":
                    handleSubscribeUser(session, request);
                    break;
                case "unsubscribe_portfolio":
                    handleUnsubscribePortfolio(session, request);
                    break;
                case "unsubscribe_user":
                    handleUnsubscribeUser(session, request);
                    break;
                case "get_portfolio":
                    handleGetPortfolio(session, request);
                    break;
                default:
                    sendError(session, "Unknown action: " + action);
            }
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status);
        
        // Remove session from all subscriptions
        portfolioSubscriptions.values().forEach(sessions -> sessions.remove(session));
        userSubscriptions.values().forEach(sessions -> sessions.remove(session));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session: {}", session.getId(), exception);
        session.close(CloseStatus.SERVER_ERROR);
    }

    // Subscription handlers

    private void handleSubscribePortfolio(WebSocketSession session, Map<String, Object> request) throws IOException {
        Long portfolioId = getLongValue(request, "portfolioId");
        
        portfolioSubscriptions
                .computeIfAbsent(portfolioId, k -> new CopyOnWriteArraySet<>())
                .add(session);
        
        log.info("Session {} subscribed to portfolio: {}", session.getId(), portfolioId);
        
        sendMessage(session, Map.of(
                "type", "subscription",
                "status", "subscribed",
                "portfolioId", portfolioId
        ));
        
        // Send initial portfolio data
        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);
            sendMessage(session, Map.of(
                    "type", "portfolio_update",
                    "data", portfolio
            ));
        } catch (Exception e) {
            log.error("Error sending initial portfolio data", e);
            sendError(session, "Error fetching portfolio: " + e.getMessage());
        }
    }

    private void handleSubscribeUser(WebSocketSession session, Map<String, Object> request) throws IOException {
        Long userId = getLongValue(request, "userId");
        
        userSubscriptions
                .computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>())
                .add(session);
        
        log.info("Session {} subscribed to user portfolios: {}", session.getId(), userId);
        
        sendMessage(session, Map.of(
                "type", "subscription",
                "status", "subscribed",
                "userId", userId
        ));
    }

    private void handleUnsubscribePortfolio(WebSocketSession session, Map<String, Object> request) throws IOException {
        Long portfolioId = getLongValue(request, "portfolioId");
        
        CopyOnWriteArraySet<WebSocketSession> sessions = portfolioSubscriptions.get(portfolioId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                portfolioSubscriptions.remove(portfolioId);
            }
        }
        
        log.info("Session {} unsubscribed from portfolio: {}", session.getId(), portfolioId);
        
        sendMessage(session, Map.of(
                "type", "subscription",
                "status", "unsubscribed",
                "portfolioId", portfolioId
        ));
    }

    private void handleUnsubscribeUser(WebSocketSession session, Map<String, Object> request) throws IOException {
        Long userId = getLongValue(request, "userId");
        
        CopyOnWriteArraySet<WebSocketSession> sessions = userSubscriptions.get(userId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                userSubscriptions.remove(userId);
            }
        }
        
        log.info("Session {} unsubscribed from user portfolios: {}", session.getId(), userId);
        
        sendMessage(session, Map.of(
                "type", "subscription",
                "status", "unsubscribed",
                "userId", userId
        ));
    }

    private void handleGetPortfolio(WebSocketSession session, Map<String, Object> request) throws IOException {
        Long portfolioId = getLongValue(request, "portfolioId");
        
        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);
            sendMessage(session, Map.of(
                    "type", "portfolio_data",
                    "data", portfolio
            ));
        } catch (Exception e) {
            log.error("Error fetching portfolio", e);
            sendError(session, "Error fetching portfolio: " + e.getMessage());
        }
    }

    // Public methods to broadcast updates

    /**
     * Broadcast portfolio update to all subscribed clients
     */
    public void broadcastPortfolioUpdate(Long portfolioId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = portfolioSubscriptions.get(portfolioId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            PortfolioDetailResponse portfolio = portfolioService.getPortfolioDetail(portfolioId);
            Map<String, Object> message = Map.of(
                    "type", "portfolio_update",
                    "data", portfolio
            );

            sessions.forEach(session -> {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    log.error("Error broadcasting to session: {}", session.getId(), e);
                }
            });

            log.debug("Broadcasted portfolio update to {} sessions", sessions.size());
        } catch (Exception e) {
            log.error("Error broadcasting portfolio update", e);
        }
    }

    /**
     * Broadcast user portfolio updates
     */
    public void broadcastUserPortfolioUpdate(Long userId) {
        CopyOnWriteArraySet<WebSocketSession> sessions = userSubscriptions.get(userId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }

        try {
            PortfolioResponse portfolio = portfolioService.getUserActivePortfolio(userId);
            Map<String, Object> message = Map.of(
                    "type", "user_portfolio_update",
                    "data", portfolio
            );

            sessions.forEach(session -> {
                try {
                    sendMessage(session, message);
                } catch (IOException e) {
                    log.error("Error broadcasting to session: {}", session.getId(), e);
                }
            });

            log.debug("Broadcasted user portfolio update to {} sessions", sessions.size());
        } catch (Exception e) {
            log.error("Error broadcasting user portfolio update", e);
        }
    }

    // Helper methods

    private void sendMessage(WebSocketSession session, Object message) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        sendMessage(session, Map.of(
                "type", "error",
                "message", error
        ));
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return Long.parseLong(value.toString());
    }
}
