package com.core.exception;

public class LuaScriptRetryableException extends LuaScriptException {
    public LuaScriptRetryableException(String msg, Throwable cause) { super(msg, cause); }
}
