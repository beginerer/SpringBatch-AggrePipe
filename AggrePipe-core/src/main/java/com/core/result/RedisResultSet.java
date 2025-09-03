package com.core.result;

import com.core.operation.Operation;

public sealed interface RedisResultSet permits RedisLongResultSet, RedisDoubleResultSet {


    int mask();

    boolean has(Operation op);

}
