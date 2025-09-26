package com.core.example.batch;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Path;
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

    private List<JoinSpec> join;


    public QuerySpec(String name) {
        this.name = name;
        this.where = new ArrayList<>();
        this.join = new ArrayList<>();
    }

    public JPAQuery<T> createQuery(JPAQueryFactory qf) {
        if(select == null || from == null )
            throw new IllegalStateException("[ERROR] select/from/cursor is empty");

        JPAQuery<T> q = qf.select(select).from(from);


        if(join !=null) {
            for(int i=0; i<join.size(); i++) {
                JoinSpec joinSpec = join.get(i);

                q.innerJoin(joinSpec.target, joinSpec.alias);
            }
        }

        if(where != null) {
            for(int i=0; i<where.size(); i++) {
                q.where(where.get(i));
            }
        }
        return q;
    }


    public QuerySpec<T> select(Expression<T> expression) {
        this.select = expression;
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


    public <P> QuerySpec<T> innerJoin(EntityPath<P> target, Path<P> alias) {
        this.join.add(new JoinSpec(target, alias));
        return this;
    }

    public class JoinSpec<P> {
        private EntityPath<P> target;
        private Path<P> alias;

        public EntityPath<?> getTarget() {
            return target;
        }

        public Path<?> getAlias() {
            return alias;
        }

        public JoinSpec(EntityPath<P> target, Path<P> alias) {
            this.target = target;
            this.alias = alias;
        }
    }

}
