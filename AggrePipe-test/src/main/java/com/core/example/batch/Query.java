package com.core.example.batch;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.List;


public class Query<T> {

    private String name;

    private Expression<T> select;

    private EntityPath<?>[] from;

    private Predicate[] where;

    private OrderSpecifier<?>[] orderBy;

    private List<EntityPath<?>> join;


    public JPAQuery<T> createQuery(JPAQueryFactory qf) {
        if(select == null || from == null)
            throw new IllegalStateException("[ERROR] select/from is empty");

        JPAQuery<T> q = qf.select(select).from(from);


        if(join !=null) {
            for(int i=0; i<join.size(); i++) {
                q.innerJoin(join.get(i));
            }
        }

        if(where != null) {
            q.where(where);
        }

        if(orderBy !=null) {
            q.orderBy(orderBy);
        }

        return q;
    }


    public Query<T> select(EntityPath<T> entityPath) {
        this.select = entityPath;
        return this;
    }

    public Query<T> from(EntityPath<?>... entityPaths) {
        this.from = entityPaths;
        return this;
    }

    public Query<T> where(Predicate[] predicates) {
        this.where = predicates;
        return this;
    }

    public Query<T> orderBy(OrderSpecifier<?>... orderSpecifiers) {
        this.orderBy = orderSpecifiers;
        return this;
    }

    public Query<T> join(EntityPath<?> entityPaths) {
        this.join.add(entityPaths);
        return this;
    }
}
