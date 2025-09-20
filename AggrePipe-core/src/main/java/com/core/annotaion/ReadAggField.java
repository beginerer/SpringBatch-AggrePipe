package com.core.annotaion;

import com.core.operation.Operation;
import com.core.operation.ValueType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <h2>Caution</h2>
 * <P><Strong>Getter method is necessary</Strong></P>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * @ReadQuery(
 *      name = "OrderAggDto",
 *      groupByKeys = {
 *          @GroupByKey(field = "memberId", type = ValueType.Long),
 *          @GroupByKey(field = "orderId", type = ValueType.Long)
 *      }
 * )
 * public record readQuery(
 *      Long memberId,
 *      Long orderId,
 *
 *      @ReadAggField(name = "amountKrw", op = Operation.SUM, type = ValueType.Long)
 *      long sum_amountKrw,
 *
 *      @ReadAggField(name = "amountKrw", op = Operation.MAX, type = ValueType.Long)
 *      long max_amountKrw,
 *
 *      @eadAggField(name = "point", op = Operation.MIN, type = ValueType.Long)
 *      long min_point
 * ) {}</pre>
 *
 *
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface ReadAggField {


    /**
     * <p><Strong>Caution:</Strong>must equals to original target fieldName</p>
     *
    * */
    String originalFieldName();

    Operation op();

    ValueType type();
}
