package com.excpetion;

public class ConnectionPoolClosedException extends IllegalStateException{


    public ConnectionPoolClosedException(String message) {
        super(message);
    }

}
