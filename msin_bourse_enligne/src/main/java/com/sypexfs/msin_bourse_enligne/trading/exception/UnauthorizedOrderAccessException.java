package com.sypexfs.msin_bourse_enligne.trading.exception;

public class UnauthorizedOrderAccessException extends TradingException {
    
    public UnauthorizedOrderAccessException(Long orderId) {
        super("Unauthorized access to order: " + orderId);
    }
    
    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}
