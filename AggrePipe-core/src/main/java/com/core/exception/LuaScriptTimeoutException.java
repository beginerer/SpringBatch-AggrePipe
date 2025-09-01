package com.core.exception;

public class LuaScriptTimeoutException extends LuaScriptRetryableException {
    public LuaScriptTimeoutException(String msg, Throwable cause) { super(msg, cause); }
}
