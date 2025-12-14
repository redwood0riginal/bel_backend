package com.sypexfs.msin_bourse_enligne.config;

import com.sypexfs.msin_bourse_enligne.market.websocket.MarketWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.trading.websocket.TradingWebSocketHandler;
import com.sypexfs.msin_bourse_enligne.portfolio.websocket.PortfolioWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final MarketWebSocketHandler marketWebSocketHandler;
    private final TradingWebSocketHandler tradingWebSocketHandler;
    private final PortfolioWebSocketHandler portfolioWebSocketHandler;
    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Market data WebSocket with JWT authentication
        registry.addHandler(marketWebSocketHandler, "/ws/market")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");
        
        // Trading WebSocket with JWT authentication
        registry.addHandler(tradingWebSocketHandler, "/ws/trading")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");
        
        // Portfolio WebSocket with JWT authentication
        registry.addHandler(portfolioWebSocketHandler, "/ws/portfolio")
                .addInterceptors(webSocketAuthInterceptor)
                .setAllowedOriginPatterns("*");
    }
}
