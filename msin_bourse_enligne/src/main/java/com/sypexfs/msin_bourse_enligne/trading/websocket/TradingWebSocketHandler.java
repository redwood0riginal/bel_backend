package com.sypexfs.msin_bourse_enligne.trading.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sypexfs.msin_bourse_enligne.trading.entity.Order;
import com.sypexfs.msin_bourse_enligne.trading.matching.MatchingResult;
import com.sypexfs.msin_bourse_enligne.trading.matching.OrderExecution;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket handler for real-time trading updates
 * Sends order updates, executions, and market data to connected clients
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradingWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    
    // User ID -> WebSocket Session mapping
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();
    
    // Symbol -> Set of WebSocket Sessions (for market data subscriptions)
    private final Map<String, Map<String, WebSocketSession>> symbolSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        
        // Extract user ID from session attributes (set during authentication)
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            log.info("User {} connected via WebSocket", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
        
        // Remove from user sessions
        Long userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
        }
        
        // Remove from symbol subscriptions
        symbolSubscriptions.values().forEach(sessions -> sessions.remove(session.getId()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.debug("Received WebSocket message: {}", payload);
        
        try {
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            
            switch (wsMessage.getType()) {
                case "SUBSCRIBE":
                    handleSubscription(session, wsMessage.getSymbol());
                    break;
                case "UNSUBSCRIBE":
                    handleUnsubscription(session, wsMessage.getSymbol());
                    break;
                case "PING":
                    sendPong(session);
                    break;
                default:
                    log.warn("Unknown message type: {}", wsMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error processing WebSocket message", e);
            sendError(session, "Invalid message format");
        }
    }

    /**
     * Send order update to user
     */
    public void sendOrderUpdate(Order order, MatchingResult result) {
        WebSocketSession session = userSessions.get(order.getUserId());
        if (session != null && session.isOpen()) {
            try {
                OrderUpdateMessage message = new OrderUpdateMessage(
                    "ORDER_UPDATE",
                    order.getId(),
                    order.getSymbol(),
                    order.getStatId(),
                    order.getQuantity(),
                    order.getExecQty(),
                    order.getRemainingQuantity(),
                    order.getPrice(),
                    order.getExecAvgPrice(),
                    result.getExecutions(),
                    LocalDateTime.now()
                );
                
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("Sent order update to user {}: order {}", order.getUserId(), order.getId());
            } catch (IOException e) {
                log.error("Failed to send order update to user {}", order.getUserId(), e);
            }
        }
    }

    /**
     * Send order cancellation to user
     */
    public void sendOrderCancellation(Order order, String reason) {
        WebSocketSession session = userSessions.get(order.getUserId());
        if (session != null && session.isOpen()) {
            try {
                OrderCancellationMessage message = new OrderCancellationMessage(
                    "ORDER_CANCELLED",
                    order.getId(),
                    order.getSymbol(),
                    reason,
                    LocalDateTime.now()
                );
                
                String json = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(json));
                log.debug("Sent order cancellation to user {}: order {}", order.getUserId(), order.getId());
            } catch (IOException e) {
                log.error("Failed to send order cancellation to user {}", order.getUserId(), e);
            }
        }
    }

    /**
     * Broadcast trade execution to all subscribers of a symbol
     */
    public void broadcastTradeExecution(String symbol, OrderExecution execution) {
        Map<String, WebSocketSession> subscribers = symbolSubscriptions.get(symbol);
        if (subscribers != null && !subscribers.isEmpty()) {
            TradeExecutionMessage message = new TradeExecutionMessage(
                "TRADE_EXECUTION",
                symbol,
                execution.getPrice(),
                execution.getQuantity(),
                execution.getAmount(),
                execution.getTimestamp()
            );
            
            try {
                String json = objectMapper.writeValueAsString(message);
                TextMessage textMessage = new TextMessage(json);
                
                subscribers.values().forEach(session -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.error("Failed to send trade execution to session {}", session.getId(), e);
                        }
                    }
                });
                
                log.debug("Broadcasted trade execution for {} to {} subscribers", symbol, subscribers.size());
            } catch (IOException e) {
                log.error("Failed to serialize trade execution message", e);
            }
        }
    }

    /**
     * Broadcast market data update to all subscribers of a symbol
     */
    public void broadcastMarketDataUpdate(String symbol, java.math.BigDecimal price, 
                                          java.math.BigDecimal volume, LocalDateTime timestamp) {
        Map<String, WebSocketSession> subscribers = symbolSubscriptions.get(symbol);
        if (subscribers != null && !subscribers.isEmpty()) {
            MarketDataMessage message = new MarketDataMessage(
                "MARKET_DATA",
                symbol,
                price,
                volume,
                timestamp
            );
            
            try {
                String json = objectMapper.writeValueAsString(message);
                TextMessage textMessage = new TextMessage(json);
                
                subscribers.values().forEach(session -> {
                    if (session.isOpen()) {
                        try {
                            session.sendMessage(textMessage);
                        } catch (IOException e) {
                            log.error("Failed to send market data to session {}", session.getId(), e);
                        }
                    }
                });
                
                log.debug("Broadcasted market data for {} to {} subscribers", symbol, subscribers.size());
            } catch (IOException e) {
                log.error("Failed to serialize market data message", e);
            }
        }
    }

    /**
     * Handle symbol subscription
     */
    private void handleSubscription(WebSocketSession session, String symbol) {
        symbolSubscriptions.computeIfAbsent(symbol, k -> new ConcurrentHashMap<>())
            .put(session.getId(), session);
        log.info("Session {} subscribed to symbol {}", session.getId(), symbol);
        
        // Send confirmation
        try {
            SubscriptionConfirmMessage message = new SubscriptionConfirmMessage(
                "SUBSCRIPTION_CONFIRMED",
                symbol,
                "subscribed"
            );
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send subscription confirmation", e);
        }
    }

    /**
     * Handle symbol unsubscription
     */
    private void handleUnsubscription(WebSocketSession session, String symbol) {
        Map<String, WebSocketSession> subscribers = symbolSubscriptions.get(symbol);
        if (subscribers != null) {
            subscribers.remove(session.getId());
            log.info("Session {} unsubscribed from symbol {}", session.getId(), symbol);
        }
        
        // Send confirmation
        try {
            SubscriptionConfirmMessage message = new SubscriptionConfirmMessage(
                "UNSUBSCRIPTION_CONFIRMED",
                symbol,
                "unsubscribed"
            );
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send unsubscription confirmation", e);
        }
    }

    /**
     * Send pong response
     */
    private void sendPong(WebSocketSession session) {
        try {
            PongMessage message = new PongMessage("PONG", LocalDateTime.now());
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send pong", e);
        }
    }

    /**
     * Send error message
     */
    private void sendError(WebSocketSession session, String error) {
        try {
            ErrorMessage message = new ErrorMessage("ERROR", error, LocalDateTime.now());
            String json = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(json));
        } catch (IOException e) {
            log.error("Failed to send error message", e);
        }
    }

    /**
     * Extract user ID from session
     */
    private Long getUserIdFromSession(WebSocketSession session) {
        Object userId = session.getAttributes().get("userId");
        return userId != null ? (Long) userId : null;
    }

    // Message DTOs
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    private static class WebSocketMessage {
        private String type;
        private String symbol;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class OrderUpdateMessage {
        private String type;
        private Long orderId;
        private String symbol;
        private String status;
        private java.math.BigDecimal totalQuantity;
        private java.math.BigDecimal executedQuantity;
        private java.math.BigDecimal remainingQuantity;
        private java.math.BigDecimal orderPrice;
        private java.math.BigDecimal avgExecutionPrice;
        private java.util.List<OrderExecution> executions;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class OrderCancellationMessage {
        private String type;
        private Long orderId;
        private String symbol;
        private String reason;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class TradeExecutionMessage {
        private String type;
        private String symbol;
        private java.math.BigDecimal price;
        private java.math.BigDecimal quantity;
        private java.math.BigDecimal amount;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class MarketDataMessage {
        private String type;
        private String symbol;
        private java.math.BigDecimal price;
        private java.math.BigDecimal volume;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class SubscriptionConfirmMessage {
        private String type;
        private String symbol;
        private String status;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class PongMessage {
        private String type;
        private LocalDateTime timestamp;
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    private static class ErrorMessage {
        private String type;
        private String message;
        private LocalDateTime timestamp;
    }
}
