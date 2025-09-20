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
 *         @GroupByKey(field = "memberId", type = ValueType.LONG),
 *         @GroupByKey(field = "orderId", type= ValueType.LONG)
 *     }
 * )
 * public record OrderAggDto(
 *     Long memberId,
 *     Long orderId,
 *     @AggField(op = {Operation.SUM, Operation.MAX} , type = ValueType.LONG) long amountKrw,
 *     @AggField(op = {Operation.MIN}, type = ValueType.LONG) long point
 * ) {}
 * }</pre>
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AggQuery {

    GroupByKey[] groupByKeys();

}
