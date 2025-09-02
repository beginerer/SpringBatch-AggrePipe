package com.core.operation;

import io.lettuce.core.ScriptOutputType;

import java.util.Arrays;
import java.util.Objects;


/**
 * <p>key : KEYS[1]=setKey, KEYS[2]=numKey</p>
 * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
 * */
public class LuaScript implements LuaOperation<String, String> {


    private final String name;

    private final String script;

    private final AggregateOutputType aggregateOutputType;

    private final String[] keys;

    private final int ttl;

    private static final int DEFAULT_TTL = 86400;

    private final boolean safetyMode;




    public LuaScript(String name, String script, String setKey, String numKey, int ttl, boolean safetyMode) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.script = Objects.requireNonNull(script, "script must not be null");
        this.aggregateOutputType = AggregateOutputType.LONG;
        Objects.requireNonNull(setKey,"setKey must not be null");
        Objects.requireNonNull(numKey, "numKey must not be null");
        this.keys = new String[]{setKey, numKey};
        this.safetyMode = safetyMode;
        this.ttl = ttl;
    }


    /**
     * <p>key : KEYS[1]=setKey, KEYS[2]=numKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>*/
    public static LuaScript max(String setKey, String numKey) {

        String name = "maxOperation";
        String script = "local idemKey   = KEYS[1]\n" +
                "local numKey    = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "local beforeStr = redis.call('GET', numKey)\n" +
                "local before = beforeStr and tonumber(beforeStr) or nil\n" +
                "\n" +
                "if added == 0 then\n" +
                "    return before\n" +
                "end\n" +
                "\n" +
                "\n" +
                "if not before or value > before then\n" +
                "    redis.call('SET', numKey, value, 'EX', ttl)   \n" +
                "    redis.call('EXPIRE', idemKey, ttl)        \n" +
                "    return value\n" +
                "else\n" +
                "    redis.call('EXPIRE', idemKey, ttl)        \n" +
                "    return before\n" +
                "end\n";

        boolean safetyMode = false;
        int ttl = LuaScript.DEFAULT_TTL;
        return new LuaScript(name, script, setKey, numKey, ttl, safetyMode);
    }


    @Override
    public String[] inputData(String... data) {
        if (data == null || data.length != 2) {
            throw new IllegalArgumentException(
                    "[ERROR] ARGV expects exactly 2 elements: [requestId, value], but got " + Arrays.toString(data)
            );
        }
        String requestId = Objects.requireNonNull(data[0], "requestId must not be null");
        String value = Objects.requireNonNull(data[1], "value must not be null");
        return new String[]{requestId, value, String.valueOf(ttl)};
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getLuaScript() {
        return script;
    }

    @Override
    public AggregateOutputType getAggregateOutputType() {
        return aggregateOutputType;
    }

    @Override
    public ScriptOutputType getScriptOutputType() {
        return aggregateOutputType.getScriptOutputType();
    }

    @Override
    public String[] getKeys() {
        return keys;
    }

    @Override
    public int getTtl() {
        return ttl;
    }


    @Override
    public boolean isSafetyMode() {
        return safetyMode;
    }

    @Override
    public String toString() {
        return "LuaScript{" +
                "name='" + name + '\'' +
                ", script='" + script + '\'' +
                ", AggregateOutputTYpe=" + aggregateOutputType +
                ", scriptOutputType=" + aggregateOutputType.getScriptOutputType() +
                ", keys=" + Arrays.toString(keys) +
                '}';
    }

}
