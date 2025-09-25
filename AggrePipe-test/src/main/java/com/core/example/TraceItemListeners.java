package com.core.example;

import org.springframework.batch.core.ItemReadListener;
import org.springframework.batch.core.ItemWriteListener;
import org.springframework.batch.item.Chunk;
import org.springframework.stereotype.Component;

@Component
public class TraceItemListeners implements ItemWriteListener<Object> {

    @Override
    public void beforeWrite(Chunk<?> items) {
        System.out.println("[READ]" + items.getItems());
    }

}
