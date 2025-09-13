package com.core;

import com.core.support.AggQueryRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;


@SpringBootTest
public class AnnotationTest {


    @Autowired
    private AggQueryRegistry registry;




    @Test
    public void test() {

        QueryDto queryDto = new QueryDto(1L, 1L, 30.2, 30L, LocalDateTime.now());

        List<ItemUnit> itemUnits = registry.extractValue(queryDto);

        for (ItemUnit itemUnit : itemUnits) {
            System.out.println(itemUnit);
        }
    }

}
