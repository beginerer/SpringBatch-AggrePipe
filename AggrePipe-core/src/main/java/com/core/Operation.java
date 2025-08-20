package com.core;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;


public class Operation {


    public final static TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    public final static int DEFAULT_TIME_VALUE = 500;



    public static BiFunction<Long,Long,Long> max = (input, cur) -> {
        if(input > cur)
            return input;
        else
            return cur;
    };

    public static BiFunction<Long,Long,Long> min = (input, cur) -> {
        if(input < cur)
            return input;
        else
            return cur;
    };







}
