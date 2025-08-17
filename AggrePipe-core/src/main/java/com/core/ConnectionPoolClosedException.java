package com.core;

public class ConnectionPoolClosedException extends IllegalStateException{


    public ConnectionPoolClosedException(String message) {
        super(message);
    }

}
