package com.sypexfs.msin_bourse_enligne.trading.exception;

public class OrderNotFoundException extends TradingException {
    
    public OrderNotFoundException(Long orderId) {
        super("Order not found with ID: " + orderId);
    }
    
    public OrderNotFoundException(String message) {
        super(message);
    }
}
