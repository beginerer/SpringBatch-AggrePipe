package com.core.example;



import com.core.support.AggQueryBindingHandler;
import jakarta.persistence.EntityManager;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;


@StepScope
@Component
public class Reader<T> extends AbstractPagingItemReader<T> {


    private int pageSize;
    private int lastSize;


    private AggQueryBindingHandler handler;

    private final EntityManager em;




    @Override
    protected void doReadPage() {

    }
}
