package com.core.example.batch;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.stereotype.Component;


@Component
@StepScope
public class QuerydslZeroOffsetItemReader<T> extends AbstractPagingItemReader<T> {

    private String name;

    private int pageSize;

    private EntityManagerFactory emf;



    private void init() {
        EntityManager em = emf.createEntityManager();


    }


    @Override
    protected void doReadPage() {
        emf.createEntityManager()
    }
}
