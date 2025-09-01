package com.core.exception;

public class LuaScriptNonRetryableException extends LuaScriptException {
  public LuaScriptNonRetryableException(String msg, Throwable cause) { super(msg, cause); }

  public LuaScriptNonRetryableException(String msg) {
    super(msg);
  }
}
