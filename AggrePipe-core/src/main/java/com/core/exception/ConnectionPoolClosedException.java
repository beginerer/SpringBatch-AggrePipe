package com.core.exception;

public class ConnectionPoolClosedException extends IllegalStateException{


    public ConnectionPoolClosedException(String message) {
        super(message);
    }

}
