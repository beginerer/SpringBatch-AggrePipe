package com.core;


import java.util.function.BiFunction;


public class Operation {



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
