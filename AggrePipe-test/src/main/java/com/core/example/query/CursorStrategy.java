package com.core.example.query;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Component;

import static com.core.example.entity.QItem.item;
import static com.core.example.entity.QUser.user;


@Component
public class CursorStrategy implements com.core.batch.CursorStrategy<Cursor> {



    @Override
    public Predicate buildCursorPredicate(Cursor t) {
        return item.itemId.gt(t.getItemId()).and(user.age.gt(t.getAge()));
    }


    @Override
    public OrderSpecifier<?>[] orderBy() {
        OrderSpecifier<Long> asc = item.itemId.asc();
        OrderSpecifier<Long> asc1 = user.age.asc();

        return new OrderSpecifier[] {asc,asc1};
    }
}
