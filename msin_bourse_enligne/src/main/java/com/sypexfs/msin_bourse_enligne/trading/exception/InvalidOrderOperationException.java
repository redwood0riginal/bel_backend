package com.sypexfs.msin_bourse_enligne.trading.exception;

public class InvalidOrderOperationException extends TradingException {
    
    public InvalidOrderOperationException(String message) {
        super(message);
    }
    
    public InvalidOrderOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
