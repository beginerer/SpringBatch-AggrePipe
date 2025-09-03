package com.core.result;


import com.core.exception.LuaScriptNonRetryableException;
import com.core.operation.Operation;
import java.util.List;


public class ResultSetFactory {



    public static RedisLongResultSet buildLong(int[] opIndex, List<String> result) {

        if(opIndex.length != result.size())
            throw new LuaScriptNonRetryableException("[ERROR] opIndex and result size is incorrect. opIndex size=%d, result size=%d".
                    formatted(opIndex.length, result.size()));

        Long sum = null;
        Long count = null;
        Long max = null;
        Long min = null;

        try {
            for(int i=0; i<opIndex.length; i++) {

                String value = result.get(i);

                if(opIndex[i] == Operation.SUM.getIndex() && value!=null) {
                    sum = Long.parseLong(value);
                }else if(opIndex[i] == Operation.COUNT.getIndex() && value!=null) {
                    count = Long.parseLong(value);
                }else if(opIndex[i] == Operation.MAX.getIndex() && value!=null) {
                    max = Long.parseLong(value);
                }else if(opIndex[i] == Operation.MIN.getIndex() && value!=null) {
                    min = Long.parseLong(value);
                }else {
                    throw new LuaScriptNonRetryableException("[ERROR] unexpected exception. opIndexValue=%s, value=%s".formatted(opIndex[i], value));
                }
            }
            return new RedisLongResultSet(sum, count, max, min);

        }catch (NumberFormatException e) {
            throw new LuaScriptNonRetryableException("[ERROR] type mismatch exception result=%s".formatted(result.toString()));
        }
    }



    public static RedisDoubleResultSet buildDouble(int[] opIndex, List<String> result) {

        if(opIndex.length != result.size())
            throw new LuaScriptNonRetryableException("[ERROR] opIndex and result size is incorrect. opIndex size=%d, result size=%d".
                    formatted(opIndex.length, result.size()));

        Double sum = null;
        Long count = null;
        Double max = null;
        Double min = null;

        try {
            for(int i=0; i<opIndex.length; i++) {
                String value = result.get(i);

                if(opIndex[i] == Operation.SUM.getIndex() && value!=null) {
                    sum = Double.parseDouble(result.get(i));
                }else if(opIndex[i] == Operation.COUNT.getIndex() && value!=null) {
                    count = Long.parseLong(result.get(i));
                }else if(opIndex[i] == Operation.MAX.getIndex() && value!=null) {
                    max = Double.parseDouble(result.get(i));
                }else if(opIndex[i] == Operation.MIN.getIndex() && value!=null) {
                    min = Double.parseDouble(result.get(i));
                }else {
                    throw new LuaScriptNonRetryableException("[ERROR] unexpected exception. opIndexValue=%s, value=%s".formatted(opIndex[i], value));
                }
            }
            return new RedisDoubleResultSet(sum, count, max, min);
        }catch (NumberFormatException e) {
            throw new LuaScriptNonRetryableException("[ERROR] type mismatch exception result=%s ".formatted(result.toString()));
        }
    }


}
