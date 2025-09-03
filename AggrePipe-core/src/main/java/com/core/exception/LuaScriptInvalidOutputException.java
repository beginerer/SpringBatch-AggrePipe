package com.core.exception;

public class LuaScriptInvalidOutputException extends LuaScriptNonRetryableException {



  public LuaScriptInvalidOutputException(String message) {
    super(message);
  }


  public LuaScriptInvalidOutputException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
