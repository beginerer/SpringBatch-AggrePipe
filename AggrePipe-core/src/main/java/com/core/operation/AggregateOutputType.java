package com.core.operation;

import io.lettuce.core.ScriptOutputType;


public enum AggregateOutputType {


    LONG(ScriptOutputType.MULTI),

    DOUBLE(ScriptOutputType.MULTI),

    BIG_DECIMAL(ScriptOutputType.MULTI);



    private final ScriptOutputType scriptOutputType;


    AggregateOutputType(ScriptOutputType scriptOutputType) {
        this.scriptOutputType = scriptOutputType;
    }

    public ScriptOutputType getScriptOutputType() {
        return scriptOutputType;
    }
}
