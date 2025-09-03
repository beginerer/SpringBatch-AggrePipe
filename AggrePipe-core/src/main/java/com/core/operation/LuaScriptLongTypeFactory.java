package com.core.operation;


/**
 * <p>int[] opIndex => </p>
* <p>SUM = 0</p>
* <p>COUNT = 1</p>
* <p>MAX = 2</p>
* <p>MIN = 3</p>*/
public class LuaScriptLongTypeFactory {


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
     * <p>return : [SUM, COUNT, MAX, MIN]</p>
     * <p>int[] opIndex = {0, 1, 2, 3}</p>
     * */
    public static LuaScript LongAsAll(String idemKey, String hKey) {
        String name = "[Long] : [SUM, COUNT, MAX, MIN]";
        int[] opIndex = {0, 1, 2, 3};
        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "\n" +
                "-- 0: sum, 1:count, 2: max, 3: min\n" +
                "local cur = redis.call('HMGET', hKey, '0', '1', '2', '3')\n" +
                "local sum = cur[1] and tonumber(cur[1]) or nil\n" +
                "local cnt = cur[2] and tonumber(cur[2]) or nil\n" +
                "local max = cur[3] and tonumber(cur[3]) or nil\n" +
                "local min = cur[4] and tonumber(cur[4]) or nil\n" +
                "\n" +
                "\n" +
                "if added == 0 then\n" +
                "    return {cur[1], cur[2], cur[3], cur[4]}\n" +
                "end\n" +
                "\n" +
                "\n" +
                "if not cnt then\n" +
                "    redis.call('HSET', hKey, '0', value, '1', 1, '2', value, '3', value)\n" +
                "\n" +
                "    if ttl and ttl > 0 then\n" +
                "        redis.call('EXPIRE', hKey, ttl)\n" +
                "        redis.call('EXPIRE', idemKey, ttl)\n" +
                "    end\n" +
                "    return {tostring(value), \"1\", tostring(value), tostring(value)}\n" +
                "end\n" +
                "\n" +
                "\n" +
                "local newSum = redis.call('HINCRBY', hKey, '0', value)\n" +
                "local newCnt = redis.call('HINCRBY', hKey, '1', 1)\n" +
                "local newMax = (value > max) and value or max\n" +
                "local newMin = (min > value) and value or min\n" +
                "\n" +
                "if max ~=newMax then\n" +
                "    redis.call('HSET', hKey, '2', newMax)\n" +
                "end\n" +
                "\n" +
                "if min ~= newMin then\n" +
                "    redis.call('HSET', hKey, '3', newMin)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return {newSum, newCnt, tostring(newMax), tostring(newMin)}";
        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
     * <p>return : [SUM, COUNT]</p>
     * <p>int[] opIndex = {0, 1}</p>
     * */
    public static LuaScript LongAsSumAndCount(String idemKey, String hKey) {
        String name = "[Long] : [SUM, COUNT]";
        int[] opIndex = {0, 1};

        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "\n" +
                "if added == 0 then\n" +
                "    local cur = redis.call('HMGET', hKey, '0', '1')\n" +
                "    return {cur[1], cur[2]}\n" +
                "end\n" +
                "\n" +
                "\n" +
                "local newSum = redis.call('HINCRBY', hKey, '0', value)\n" +
                "local newCnt =redis.call('HINCRBY', hKey, '1', 1)\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return {tostring(newSum), tostring(newCnt)}";
        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
     * <p>return : [SUM]</p>
     * <p>int[] opIndex = {0}</p>
     * */
    public static LuaScript LongAsSUM(String idemKey, String hKey) {
        String name = "[LONG] : [SUM]";
        int[] opIndex = {0};

        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "\n" +
                "-- 0: sum, 1:count, 2: max, 3: min\n" +
                "\n" +
                "if added == 0 then\n" +
                "    local cur = redis.call('HGET', hKey, '0')\n" +
                "    return {cur and tostring(cur) or nil}\n" +
                "end\n" +
                "\n" +
                "local newSum = redis.call('HINCRBY', hKey, '0', value)\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return {tostring(newSum)}";

        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value(not-used), ARGV[3]=ttl</p>
     * <p>return : [COUNT]</p>
     * <p>int[] opIndex = {1}</p>
     * */
    public static LuaScript LongAsCnt(String idemKey, String hKey) {
        String name = "[LONG] : [COUNT]";
        int[] opIndex = {1};

        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "\n" +
                "-- 0: sum, 1:count, 2: max, 3: min\n" +
                "\n" +
                "if added == 0 then\n" +
                "    local cur = redis.call('HGET', hKey, '1')\n" +
                "    return {cur and tostring(cur) or nil}\n" +
                "end\n" +
                "\n" +
                "\n" +
                "local newCnt = redis.call('HINCRBY', hKey, '1', 1)\n" +
                "\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return { tostring(newCnt) }";

        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
     * <p>return : [MAX]</p>
     * <p>int[] opIndex = {2}</p>
     * */
    public static LuaScript LongAsMax(String idemKey, String hKey) {
        String name = "[LONG] : [MAX]";
        int[] opIndex = {2};
        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "local cur = redis.call('HGET', hKey, '2')\n" +
                "local max = cur and tonumber(cur) or nil\n" +
                "\n" +
                "-- 0: sum, 1:count, 2: max, 3: min\n" +
                "\n" +
                "if added == 0 then\n" +
                "    return {cur}\n" +
                "end\n" +
                "\n" +
                "if not max then\n" +
                "    redis.call('HSET', hKey, '2', value)\n" +
                "    if ttl and ttl > 0 then\n" +
                "        redis.call('EXPIRE', hKey, ttl)\n" +
                "        redis.call('EXPIRE', idemKey, ttl)\n" +
                "    end\n" +
                "    return {tostring(value)}\n" +
                "end\n" +
                "\n" +
                "local newMax = (value > max) and value or max;\n" +
                "\n" +
                "if max ~=newMax then\n" +
                "    redis.call('HSET', hKey, '2', newMax)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return {tostring(newMax)}";
        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }


    /**
     * <p>key : KEYS[1]=idemKey, KEYS[2]=hKey</p>
     * <p>argv: ARGV[1]=requestId, ARGV[2]=value, ARGV[3]=ttl</p>
     * <p>return : [MIN]</p>
     * <p>int[] opIndex = {3}</p>
     * */
    public static LuaScript LongAsMin(String idemKey, String hKey) {
        String name = "[LONG] : [MIN]";
        int[] opIndex = {3};
        String script = "local idemKey   = KEYS[1]\n" +
                "local hKey      = KEYS[2]\n" +
                "local requestId = ARGV[1]\n" +
                "local value     = tonumber(ARGV[2])\n" +
                "local ttl       = tonumber(ARGV[3])\n" +
                "\n" +
                "\n" +
                "local added = redis.call('SADD', idemKey, requestId)\n" +
                "local cur = redis.call('HGET', hKey, '3')\n" +
                "local min = cur and tonumber(cur) or nil\n" +
                "\n" +
                "-- 0: sum, 1:count, 2: max, 3: min\n" +
                "\n" +
                "if added == 0 then\n" +
                "    return {cur}\n" +
                "end\n" +
                "\n" +
                "if not min then\n" +
                "    redis.call('HSET', hKey, '3', value)\n" +
                "    if ttl and ttl > 0 then\n" +
                "        redis.call('EXPIRE', hKey, ttl)\n" +
                "        redis.call('EXPIRE', idemKey, ttl)\n" +
                "    end\n" +
                "    return {tostring(value)}\n" +
                "end\n" +
                "\n" +
                "local newMin = (min > value) and value or min;\n" +
                "\n" +
                "if min ~=newMin then\n" +
                "    redis.call('HSET', hKey, '3', newMin)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "if ttl and ttl > 0 then\n" +
                "    redis.call('EXPIRE', hKey, ttl)\n" +
                "    redis.call('EXPIRE', idemKey, ttl)\n" +
                "end\n" +
                "\n" +
                "\n" +
                "return {tostring(newMin)}";
        int ttl = LuaScript.DEFAULT_TTL;
        boolean safetyMode = false;
        AggregateOutputType outputType = AggregateOutputType.LONG;
        return new LuaScript(name, script, opIndex, idemKey, hKey, ttl, safetyMode, outputType);
    }
}
