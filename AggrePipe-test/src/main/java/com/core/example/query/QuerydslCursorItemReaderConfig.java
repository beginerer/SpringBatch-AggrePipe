package com.core.example.query;


import com.core.example.dto.ItemSaleForUserQueryDto;
import com.core.support.AggQueryBindingHandler;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import static com.core.example.entity.QItem.item;
import static com.core.example.entity.QOrderItem.orderItem;
import static com.core.example.entity.QOrders.orders;
import static com.core.example.entity.QUser.user;



@Component
public class QuerydslCursorItemReaderConfig {

    @Autowired
    private JPAQueryFactory factory;

    @Autowired
    private AggQueryBindingHandler handler;

    @Autowired
    private CursorStrategy<Cursor> cursorSterategy;

    @Autowired
    private KeyExtractor<ItemSaleForUserQueryDto, Cursor> keyExtractor;



    public QuerydslCursorItemReader<ItemSaleForUserQueryDto, Cursor> buildReader(String serialNumber, String name, int chunkSize,
                                                                                 LocalDateTime from, LocalDateTime to) {

        QuerySpec<ItemSaleForUserQueryDto> test = new QuerySpec<>("test");

        QuerySpec<ItemSaleForUserQueryDto> query = test.select(Projections.constructor(ItemSaleForUserQueryDto.class, orderItem.orderItemId,
                        orders.orderId, item.itemId, item.name, user.age, orderItem.quantity, orders.price))
                .from(orderItem)
                .innerJoin(orderItem.item, item)
                .innerJoin(orderItem.orders, orders)
                .innerJoin(orders.user, user)
                .where(orders.createdAt.between(from, to));

        return new QuerydslCursorItemReader<>(serialNumber, name, factory, query, cursorSterategy, keyExtractor, chunkSize, handler );
    }


}
