package com.core;

import com.core.operation.Operation;
import com.core.operation.ValueType;

import java.util.Arrays;


public class ItemUnit {


    private String fieldName;

    private Operation[] operations;

    private ValueType valueType;

    private Long lv;

    private Double dv;

    private boolean preCalculate;

    private Calculation calculation;



    public ItemUnit(String fieldName, Operation[] operations, ValueType valueType, Long lv, Double dv) {
        this.fieldName = fieldName;
        this.operations = operations;
        this.valueType = valueType;
        this.lv = lv;
        this.dv = dv;
        this.preCalculate = false;
        this.calculation = null;
    }

    public ItemUnit(String fieldName, Operation[] operations, ValueType valueType, Long lv, Double dv, boolean preCalculate, Calculation calculation) {
        this.fieldName = fieldName;
        this.operations = operations;
        this.valueType = valueType;
        this.lv = lv;
        this.dv = dv;
        this.preCalculate = preCalculate;
        this.calculation = calculation;
    }



    public String getFieldName() {
        return fieldName;
    }

    public Operation[] getOperations() {
        return operations;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public Long getLv() {
        return lv;
    }

    public Double getDv() {
        return dv;
    }

    public boolean isPreCalculate() {
        return preCalculate;
    }

    public Calculation getCalculation() {
        return calculation;
    }


    @Override
    public String toString() {
        return "ItemUnit{" +
                "fieldName='" + fieldName + '\'' +
                ", operations=" + Arrays.toString(operations) +
                ", valueType=" + valueType +
                ", lv=" + lv +
                ", dv=" + dv +
                ", preCalculate=" + preCalculate +
                ", calculation=" + calculation +
                '}';
    }




    public static class Calculation {
        private Long sum_lv;
        private Long max_lv;
        private Long min_lv;

        private Double sum_dv;
        private Double max_dv;
        private Double min_dv;


        public Calculation(Long sum_lv, Long max_lv, Long min_lv, Double sum_dv, Double max_dv, Double min_dv) {
            this.sum_lv = sum_lv;
            this.max_lv = max_lv;
            this.min_lv = min_lv;
            this.sum_dv = sum_dv;
            this.max_dv = max_dv;
            this.min_dv = min_dv;
        }

        public Long getSum_lv() {
            return sum_lv;
        }

        public Long getMax_lv() {
            return max_lv;
        }

        public Long getMin_lv() {
            return min_lv;
        }

        public Double getSum_dv() {
            return sum_dv;
        }

        public Double getMax_dv() {
            return max_dv;
        }

        public Double getMin_dv() {
            return min_dv;
        }

    }
}
