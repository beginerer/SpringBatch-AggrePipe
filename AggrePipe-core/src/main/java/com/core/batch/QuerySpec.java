package com.core.batch;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.ArrayList;
import java.util.List;


public class QuerySpec<T> {

    private String name;

    private Expression<T> select;

    private EntityPath<?> from;

    private List<Predicate> where;

    private Predicate cursor;

    private List<EntityPath<?>> join;


    public QuerySpec(String name) {
        this.name = name;
        this.where = new ArrayList<>();
        this.join = new ArrayList<>();
    }

    public JPAQuery<T> createQuery(JPAQueryFactory qf) {
        if(select == null || from == null || cursor == null)
            throw new IllegalStateException("[ERROR] select/from/cursor is empty");

        JPAQuery<T> q = qf.select(select).from(from);


        if(join !=null) {
            for(int i=0; i<join.size(); i++) {
                q.innerJoin(join.get(i));
            }
        }

        if(where != null) {
            for(int i=0; i<where.size(); i++) {
                q.where(where.get(i));
            }
        }
        return q;
    }


    public QuerySpec<T> select(EntityPath<T> entityPath) {
        this.select = entityPath;
        return this;
    }

    public QuerySpec<T> from(EntityPath<?> entityPath) {
        this.from = entityPath;
        return this;
    }

    public QuerySpec<T> where(Predicate predicate) {
        this.where.add(predicate);
        return this;
    }

    public QuerySpec<T> cursor(Predicate predicate) {
        this.cursor = predicate;
        return this;
    }

    public QuerySpec<T> innerJoin(EntityPath<?> entityPaths) {
        this.join.add(entityPaths);
        return this;
    }

}
