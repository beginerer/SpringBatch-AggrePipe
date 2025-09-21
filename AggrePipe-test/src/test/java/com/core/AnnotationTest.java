package com.core;

import com.core.operation.ValueType;
import com.core.support.AggQueryRegistry;
import com.core.support.ReadQueryBindingHandler;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@SpringBootTest
public class AnnotationTest {


    @Autowired
    private AggQueryRegistry registry;

    @Autowired
    private ReadQueryBindingHandler handler;




    @Test
    public void test() {

        QueryDto queryDto = new QueryDto(1L, 1L, 30.2, 30L, LocalDateTime.now());

        List<ItemUnit> itemUnits = registry.extractValue(queryDto);

        for (ItemUnit itemUnit : itemUnits) {
            System.out.println(itemUnit);
        }
    }



}
