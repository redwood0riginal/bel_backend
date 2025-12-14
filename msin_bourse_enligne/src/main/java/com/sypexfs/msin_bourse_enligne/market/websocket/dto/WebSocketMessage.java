package com.sypexfs.msin_bourse_enligne.market.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;      // "subscribe", "unsubscribe", "ping", "request"
    private String channel;   // "market.summary", "market.orderbook", "market.transactions", etc.
    private String symbol;    // Optional: specific symbol to subscribe to
    private Object data;      // Optional: additional data
}
