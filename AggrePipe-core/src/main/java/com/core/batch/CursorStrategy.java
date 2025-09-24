package com.core.batch;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;



public interface CursorStrategy<K> {


    Predicate buildCursorPredicate(K t);

    OrderSpecifier<?>[] orderBy();
}
