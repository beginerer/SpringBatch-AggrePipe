package com.core.exception;

public class LuaScriptException extends RuntimeException {
    public LuaScriptException(String msg) { super(msg); }
    public LuaScriptException(String msg, Throwable cause) { super(msg, cause); }
}
