package com.core.batch;

import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.support.AbstractItemStreamItemWriter;

public class QueryDslCursorItemWriter<T> extends AbstractItemStreamItemWriter<T> {


    @Override
    public void write(Chunk<? extends T> chunk) throws Exception {

    }
}
