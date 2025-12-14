package com.sypexfs.msin_bourse_enligne.market.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sypexfs.msin_bourse_enligne.market.dto.*;
import com.sypexfs.msin_bourse_enligne.market.service.MarketService;
import com.sypexfs.msin_bourse_enligne.market.websocket.dto.WebSocketMessage;
import com.sypexfs.msin_bourse_enligne.market.websocket.dto.WebSocketResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarketWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final MarketService marketService;
    private final MarketMapper marketMapper;

    // Session management
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    
    // Subscription management: channel -> set of session IDs
    private final Map<String, Set<String>> channelSubscriptions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {} from {}", session.getId(), session.getRemoteAddress());
        
        // Send welcome message
        WebSocketResponse welcome = WebSocketResponse.builder()
                .type("connection")
                .message("Connected to market data WebSocket")
                .data(Map.of(
                    "sessionId", session.getId(),
                    "availableChannels", getAvailableChannels()
                ))
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, welcome);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message from {}: {}", session.getId(), payload);
            
            WebSocketMessage wsMessage = objectMapper.readValue(payload, WebSocketMessage.class);
            
            switch (wsMessage.getType().toLowerCase()) {
                case "subscribe":
                    handleSubscribe(session, wsMessage);
                    break;
                case "unsubscribe":
                    handleUnsubscribe(session, wsMessage);
                    break;
                case "request":
                    handleRequest(session, wsMessage);
                    break;
                case "ping":
                    handlePing(session);
                    break;
                default:
                    sendError(session, "Unknown message type: " + wsMessage.getType());
            }
        } catch (Exception e) {
            log.error("Error handling message from {}: {}", session.getId(), e.getMessage());
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // Remove from all subscriptions
        channelSubscriptions.values().forEach(subscribers -> subscribers.remove(sessionId));
        
        log.info("WebSocket connection closed: {} with status: {}", sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    // ==================== Message Handlers ====================

    private void handleSubscribe(WebSocketSession session, WebSocketMessage message) throws IOException {
        String channel = message.getChannel();
        String symbol = message.getSymbol();
        
        if (channel == null) {
            sendError(session, "Channel is required for subscription");
            return;
        }
        
        String fullChannel = symbol != null ? channel + ":" + symbol : channel;
        
        channelSubscriptions.computeIfAbsent(fullChannel, k -> new CopyOnWriteArraySet<>())
                .add(session.getId());
        
        log.info("Session {} subscribed to {}", session.getId(), fullChannel);
        
        // Send confirmation
        WebSocketResponse response = WebSocketResponse.builder()
                .type("subscribed")
                .channel(fullChannel)
                .message("Successfully subscribed to " + fullChannel)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, response);
        
        // Send initial data
        sendInitialData(session, channel, symbol);
    }

    private void handleUnsubscribe(WebSocketSession session, WebSocketMessage message) throws IOException {
        String channel = message.getChannel();
        String symbol = message.getSymbol();
        String fullChannel = symbol != null ? channel + ":" + symbol : channel;
        
        Set<String> subscribers = channelSubscriptions.get(fullChannel);
        if (subscribers != null) {
            subscribers.remove(session.getId());
        }
        
        log.info("Session {} unsubscribed from {}", session.getId(), fullChannel);
        
        WebSocketResponse response = WebSocketResponse.builder()
                .type("unsubscribed")
                .channel(fullChannel)
                .message("Successfully unsubscribed from " + fullChannel)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, response);
    }

    private void handleRequest(WebSocketSession session, WebSocketMessage message) throws IOException {
        String channel = message.getChannel();
        String symbol = message.getSymbol();
        
        Object data = null;
        
        switch (channel) {
            case "market.overview":
                data = getMarketOverview();
                break;
            case "market.summary":
                if (symbol != null) {
                    data = marketService.getLatestSummaryBySymbol(symbol)
                            .map(marketMapper::toMarketSummaryDto)
                            .orElse(null);
                } else {
                    data = marketMapper.toMarketSummaryDtoList(marketService.getAllSummaries());
                }
                break;
            case "market.orderbook":
                if (symbol != null) {
                    data = Map.of(
                        "buy", marketMapper.toOrderbookDtoList(marketService.getBuyOrdersBySymbol(symbol)),
                        "sell", marketMapper.toOrderbookDtoList(marketService.getSellOrdersBySymbol(symbol))
                    );
                }
                break;
            case "market.transactions":
                if (symbol != null) {
                    data = marketMapper.toTransactionDtoList(marketService.getTransactionsBySymbol(symbol, 50));
                } else {
                    data = marketMapper.toTransactionDtoList(marketService.getRecentTransactions(50));
                }
                break;
            case "market.indices":
                data = marketMapper.toIndexSummaryDtoList(marketService.getIndexOverview());
                break;
            case "market.news":
                data = marketMapper.toNewsDtoList(marketService.getRecentNews(20));
                break;
            default:
                sendError(session, "Unknown channel: " + channel);
                return;
        }
        
        WebSocketResponse response = WebSocketResponse.builder()
                .type("data")
                .channel(channel + (symbol != null ? ":" + symbol : ""))
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, response);
    }

    private void handlePing(WebSocketSession session) throws IOException {
        WebSocketResponse response = WebSocketResponse.builder()
                .type("pong")
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, response);
    }

    // ==================== Broadcasting ====================

    public void broadcastMarketSummary(MarketSummaryDto summary) {
        String channel = "market.summary:" + summary.getSymbol();
        broadcast(channel, "data", summary);
        
        // Also broadcast to general market.summary channel
        broadcast("market.summary", "data", summary);
    }

    public void broadcastOrderbook(String symbol, Map<String, Object> orderbook) {
        broadcast("market.orderbook:" + symbol, "data", orderbook);
    }

    public void broadcastTransaction(TransactionDto transaction) {
        String channel = "market.transactions:" + transaction.getSymbol();
        broadcast(channel, "data", transaction);
        
        // Also broadcast to general transactions channel
        broadcast("market.transactions", "data", transaction);
    }

    public void broadcastIndexUpdate(IndexSummaryDto index) {
        broadcast("market.indices", "data", index);
    }

    public void broadcastMarketOverview(Object overview) {
        broadcast("market.overview", "data", overview);
    }

    public void broadcastNews(NewsDto news) {
        broadcast("market.news", "data", news);
    }

    public void broadcastMarketStatus(String status, String message) {
        broadcast("market.status", "status", Map.of(
            "status", status,
            "message", message
        ));
    }

    public void broadcast(String channel, String type, Object data) {
        Set<String> subscribers = channelSubscriptions.get(channel);
        if (subscribers == null || subscribers.isEmpty()) {
            return;
        }
        
        WebSocketResponse response = WebSocketResponse.builder()
                .type(type)
                .channel(channel)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
        
        subscribers.forEach(sessionId -> {
            WebSocketSession session = sessions.get(sessionId);
            if (session != null && session.isOpen()) {
                try {
                    sendMessage(session, response);
                } catch (IOException e) {
                    log.error("Error broadcasting to session {}: {}", sessionId, e.getMessage());
                }
            }
        });
    }

    // ==================== Helper Methods ====================

    private void sendInitialData(WebSocketSession session, String channel, String symbol) throws IOException {
        Object data = null;
        
        switch (channel) {
            case "market.summary":
                if (symbol != null) {
                    data = marketService.getLatestSummaryBySymbol(symbol)
                            .map(marketMapper::toMarketSummaryDto)
                            .orElse(null);
                }
                break;
            case "market.orderbook":
                if (symbol != null) {
                    data = Map.of(
                        "buy", marketMapper.toOrderbookDtoList(marketService.getBuyOrdersBySymbol(symbol)),
                        "sell", marketMapper.toOrderbookDtoList(marketService.getSellOrdersBySymbol(symbol))
                    );
                }
                break;
            case "market.transactions":
                if (symbol != null) {
                    data = marketMapper.toTransactionDtoList(marketService.getTransactionsBySymbol(symbol, 20));
                }
                break;
        }
        
        if (data != null) {
            WebSocketResponse response = WebSocketResponse.builder()
                    .type("data")
                    .channel(channel + (symbol != null ? ":" + symbol : ""))
                    .data(data)
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            sendMessage(session, response);
        }
    }

    private MarketOverviewDto getMarketOverview() {
        return MarketOverviewDto.builder()
                .indices(marketMapper.toIndexSummaryDtoList(marketService.getIndexOverview()))
                .topGainers(marketMapper.toMarketSummaryDtoList(marketService.getTopGainers(10)))
                .topLosers(marketMapper.toMarketSummaryDtoList(marketService.getTopLosers(10)))
                .mostActive(marketMapper.toMarketSummaryDtoList(marketService.getMostActive(10)))
                .build();
    }

    private Set<String> getAvailableChannels() {
        return Set.of(
            "market.summary",
            "market.orderbook",
            "market.transactions",
            "market.indices",
            "market.news",
            "market.overview",
            "market.status"
        );
    }

    private void sendMessage(WebSocketSession session, WebSocketResponse response) throws IOException {
        if (session.isOpen()) {
            String json = objectMapper.writeValueAsString(response);
            session.sendMessage(new TextMessage(json));
        }
    }

    private void sendError(WebSocketSession session, String error) throws IOException {
        WebSocketResponse response = WebSocketResponse.builder()
                .type("error")
                .message(error)
                .timestamp(System.currentTimeMillis())
                .build();
        
        sendMessage(session, response);
    }

    // ==================== Statistics ====================

    public int getActiveSessionsCount() {
        return sessions.size();
    }

    public Map<String, Integer> getChannelSubscriptionCounts() {
        Map<String, Integer> counts = new ConcurrentHashMap<>();
        channelSubscriptions.forEach((channel, subscribers) -> 
            counts.put(channel, subscribers.size())
        );
        return counts;
    }
}
