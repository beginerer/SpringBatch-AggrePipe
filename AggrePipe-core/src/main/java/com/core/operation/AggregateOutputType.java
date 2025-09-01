package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.math.BigDecimal;

public enum AggregateOutputType {

    LONG(Long.class, ScriptOutputType.INTEGER),
    DOUBLE(Double.class, ScriptOutputType.VALUE),
    BIG_DECIMAL(BigDecimal.class, ScriptOutputType.VALUE);


    private final Class<?> returnType;

    private final ScriptOutputType scriptOutputType;


    AggregateOutputType(Class<?> returnType, ScriptOutputType scriptOutputType) {
        this.returnType = returnType;
        this.scriptOutputType = scriptOutputType;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public ScriptOutputType getScriptOutputType() {
        return scriptOutputType;
    }
}
