package com.core.operation;

public interface DigestLuaOperation<T,V> extends LuaOperation<T, V> {


    String getDigestScript();

}
