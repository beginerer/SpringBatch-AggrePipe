package com.core.example;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PutMapping;

@Repository
@RequiredArgsConstructor
public class Repos {


    private final JPAQueryFactory queryFactory;


    public void hell() {
        queryFactory.select()
    }






}
