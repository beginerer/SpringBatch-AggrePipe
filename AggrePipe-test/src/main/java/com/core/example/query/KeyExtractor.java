package com.core.example.query;

import com.core.example.dto.ItemSaleForUserQueryDto;
import org.springframework.stereotype.Component;


@Component
public class KeyExtractor implements com.core.batch.KeyExtractor<ItemSaleForUserQueryDto, Cursor> {


    @Override
    public Cursor extract(ItemSaleForUserQueryDto dto) {
        return new Cursor(dto.getItemId(), dto.getAge());
    }




}
