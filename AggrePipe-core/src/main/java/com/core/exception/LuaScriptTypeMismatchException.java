package com.core.exception;

public class LuaScriptTypeMismatchException extends LuaScriptNonRetryableException {



  public LuaScriptTypeMismatchException(String message) {
    super(message);
  }


  public LuaScriptTypeMismatchException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
