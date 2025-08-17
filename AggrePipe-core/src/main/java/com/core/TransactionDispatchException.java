package com.core;

public class TransactionDispatchException extends RuntimeException {

    public TransactionDispatchException(String message) {
        super(message);
    }

    public TransactionDispatchException(String message, Throwable cause) {
        super(message, cause);
    }
}
