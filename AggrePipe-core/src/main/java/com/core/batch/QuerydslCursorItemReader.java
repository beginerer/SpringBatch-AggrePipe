package com.core.batch;


import com.core.support.AggQueryBindingHandler;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.batch.item.*;
import org.springframework.batch.item.support.AbstractItemStreamItemReader;

import java.util.List;



public class QuerydslCursorItemReader<T, K> extends AbstractItemStreamItemReader<AggQueryBindingHandler.Key> {

    private String name;

    private JPAQueryFactory qf;

    private QuerySpec<T> querySpec;

    private CursorStrategy<K> cursorStrategy;

    private KeyExtractor<T, K> keyExtractor;

    private K lastKey;

    private int chunkSize;

    private AggQueryBindingHandler handler;

    private String serialNumber;



    public QuerydslCursorItemReader(String serialNumber, String name, JPAQueryFactory qf, QuerySpec<T> querySpec, CursorStrategy<K> cursorStrategy,
                                    KeyExtractor<T, K> keyExtractor, int chunkSize, AggQueryBindingHandler handler) {
        this.name = name;
        this.qf = qf;
        this.querySpec = querySpec;
        this.cursorStrategy = cursorStrategy;
        this.keyExtractor = keyExtractor;
        this.chunkSize = chunkSize;
        this.handler = handler;
        this.serialNumber = serialNumber;
    }


    @Override
    public AggQueryBindingHandler.Key read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        List<T> rows = fetchNextPage();

        if(rows == null || rows.isEmpty())
            return null;

        int size = rows.size();
        String token =handler.createToken();
        handler.store(serialNumber, token, rows);

        lastKey = keyExtractor.extract(rows.get(size - 1));

        return new AggQueryBindingHandler.Key(serialNumber, token);
    }

    private List<T> fetchNextPage() {
        JPAQuery<T> query = querySpec.createQuery(qf);

        Predicate cursor = cursorStrategy.buildCursorPredicate(lastKey);
        OrderSpecifier<?>[] order = cursorStrategy.orderBy();

        if(cursor != null)
            query.where(cursor);

        return query.orderBy(order).limit(chunkSize).fetch();
    }

}
