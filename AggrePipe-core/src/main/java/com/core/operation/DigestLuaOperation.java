package com.core.operation;

public interface DigestLuaOperation<T, V, U> extends LuaOperation<T, V, U> {


    String getDigestScript();


}
