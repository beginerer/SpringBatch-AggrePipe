package com.core.operation;

import java.util.Objects;

public class LuaScriptFactory {



    /**
     * <p>get sum : "SUM:{fieldName}"</p>
     * <p>get max : "MAX:{filedName}"</p>
     * <P>get min : "MIN:{fieldName}"</P>
     * <P>get count : "_meta:count{groupByKey}"</P>
    * */
    public static LuaScript create(String serial_number, String idemKey, int ttl) {
        Objects.requireNonNull(serial_number);
        Objects.requireNonNull(idemKey);

        String name = "luaScript";
        String script = "local idemKey = KEYS[1]\n" +
                "local ttl = ARGV[1]\n" +
                "local payload = cjson.decode(ARGV[2])\n" +
                "\n" +
                "\n" +
                "local function hmax(key, field, v)\n" +
                "  local cur = redis.call('HGET', key, field)\n" +
                "  local nv = tonumber(cur)\n" +
                "  local pv = tonumber(v)\n" +
                "  if nv then\n" +
                "    if nv < pv then\n" +
                "      redis.call('HSET', key, field, pv)\n" +
                "    end\n" +
                "  else\n" +
                "    redis.call('HSET', key, field, pv)\n" +
                "  end\n" +
                "end\n" +
                "\n" +
                "\n" +
                "local function hmin(key, field, v)\n" +
                "  local cur = redis.call('HGET', key, field)\n" +
                "  local nv = tonumber(cur)\n" +
                "  local pv = tonumber(v)\n" +
                "  if nv then\n" +
                "    if nv > pv then\n" +
                "      redis.call('HSET', key, field, pv)\n" +
                "    end\n" +
                "  else\n" +
                "    redis.call('HSET', key, field, pv)\n" +
                "  end\n" +
                "end\n" +
                "\n" +
                "\n" +
                "for i = 1, #payload.data do\n" +
                "  local chunk = payload.data[i]\n" +
                "  local token = tostring(chunk.token)\n" +
                "  local added = redis.call('SADD', idemKey, token)\n" +
                "  redis.call('EXPIRE', idemKey, ttl)\n" +
                "\n" +
                "  if added ~= 0 then\n" +
                "    local items = chunk.items\n" +
                "    local counts = chunk.counts\n" +
                "\n" +
                "    for groupKey, cnt in pairs(counts) do\n" +
                "      redis.call('HINCRBY', groupKey, '_meta:count', tonumber(cnt) or 1)\n" +
                "      redis.call('EXPIRE', groupKey, ttl)\n" +
                "    end\n" +
                "\n" +
                "\n" +
                "    for groupKey, itemUnits in pairs(items) do\n" +
                "      for j = 1, #itemUnits do\n" +
                "        local itemUnit  = itemUnits[j]\n" +
                "\n" +
                "        local fieldName = itemUnit.fieldName\n" +
                "        local sum_fieldName = \"SUM:\"..fieldName\n" +
                "        local max_fieldName = \"MAX:\"..fieldName\n" +
                "        local min_fieldName = \"MIN:\"..fieldName\n" +
                "\n" +
                "\n" +
                "        local operations = itemUnit.operations\n" +
                "        local vtype = itemUnit.valueType\n" +
                "        local cal = itemUnit.calculation\n" +
                "\n" +
                "        if vtype == \"LONG\" then\n" +
                "          for k = 1, #operations do\n" +
                "            local op = operations[k]\n" +
                "\n" +
                "            if op == \"SUM\" then\n" +
                "              redis.call('HINCRBY', groupKey, sum_fieldName, tonumber(cal.sum_lv))\n" +
                "            elseif op == \"MAX\" then\n" +
                "              hmax(groupKey, max_fieldName, cal.max_lv)\n" +
                "            elseif op == \"MIN\" then\n" +
                "              hmin(groupKey, min_fieldName, cal.min_lv)\n" +
                "            end\n" +
                "          end\n" +
                "        elseif vtype == \"DOUBLE\" then\n" +
                "          for k = 1, #operations do\n" +
                "            local op = operations[k]\n" +
                "\n" +
                "            if op == \"SUM\" then\n" +
                "              redis.call('HINCRBYFLOAT', groupKey, sum_fieldName, tonumber(cal.sum_dv))\n" +
                "            elseif op == \"MAX\" then\n" +
                "              hmax(groupKey, max_fieldName, tonumber(cal.max_dv))\n" +
                "            elseif op == \"MIN\" then\n" +
                "              hmin(groupKey, min_fieldName, tonumber(cal.min_dv))\n" +
                "            end\n" +
                "          end\n" +
                "        end\n" +
                "      end\n" +
                "    end\n" +
                "  end\n" +
                "end\n" +
                "\n" +
                "return redis.status_reply('OK')";

        return new LuaScript(name, serial_number, script, idemKey, ttl);
    }


    // Map<String, List<String>>
    public static LuaScriptForReading create(String serialNumber) {
        String name = "luaScriptForReading";
        String script = "\n" +
                "\n" +
                "local result = {}\n" +
                "for i = 1, #ARGV do\n" +
                "  local k = ARGV[i]\n" +
                "  result[k] = redis.call('HGETALL', k)\n" +
                "end\n" +
                "return cjson.encode(result)";

        return new LuaScriptForReading(serialNumber,script,name);
    }




}
