package com.sypexfs.msin_bourse_enligne.market.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketResponse {
    private String type;        // "connection", "subscribed", "unsubscribed", "data", "error", "pong"
    private String channel;     // Channel name
    private String message;     // Status or error message
    private Object data;        // Actual data payload
    private Long timestamp;     // Server timestamp
}
