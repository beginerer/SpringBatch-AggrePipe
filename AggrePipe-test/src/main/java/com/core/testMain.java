package com.core;


import com.core.annotaion.EnableAggQuery;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAggQuery
@SpringBootApplication
public class testMain {

    public static void main(String[] args) {
        SpringApplication.run(testMain.class, args);
    }


}
