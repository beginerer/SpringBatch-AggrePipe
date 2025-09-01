package com.core.exception;

public class LuaScriptInterruptedException extends LuaScriptRetryableException {


  public LuaScriptInterruptedException(String msg, Throwable cause) { super(msg, cause); }
}
