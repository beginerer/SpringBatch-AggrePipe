package com.core.annotaion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;



/**
 * <p>Each {@link GroupByKey} listed in {@link #groupByKeys()} MUST correspond to a
 * declared field on the annotated DTO.</p>
 * <h2>Caution</h2>
 * <P><Strong>Getter method is necessary</Strong></P>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @AggQuery(
 *     name = "OrderAggByMemberAndDate",
 *     groupByKeys = {
 *         @GroupByKey(field = "memberId"),
 *         @GroupByKey(field = "orderDate")
 *     }
 * )
 * public record OrderAggDto(
 *     Long memberId,
 *     LocalDate orderDate,
 *     @AggField(op = Operation.SUM, type = ValueType.LONG) long amountKrw,
 *     @AggField(op = Operation.COUNT, type = ValueType.LONG) long cnt
 * ) {}
 * }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggQuery {


    String name() default "";


    GroupByKey[] groupByKeys();

}
