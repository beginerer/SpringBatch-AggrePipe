package com.core;

import com.core.operation.Operation;
import com.core.operation.ValueType;

import java.util.Arrays;


public class ItemUnit {


    private String fieldName;

    private int mask;

    private ValueType valueType;

    private long lv;

    private double dv;

    private boolean preCalculate;

    private Calculation calculation;



    public ItemUnit(String fieldName, Operation[] operations, ValueType valueType, long lv, double dv) {
        this.fieldName = fieldName;
        this.mask = Operation.resolveMasking(operations);
        this.valueType = valueType;
        this.lv = lv;
        this.dv = dv;
        this.preCalculate = false;
        this.calculation = null;
    }

    public ItemUnit(String fieldName, int mask, ValueType valueType, long lv, double dv, boolean preCalculate, Calculation calculation) {
        this.fieldName = fieldName;
        this.mask = mask;
        this.valueType = valueType;
        this.lv = lv;
        this.dv = dv;
        this.preCalculate = preCalculate;
        this.calculation = calculation;
    }



    public String getFieldName() {
        return fieldName;
    }

    public int getMask() {
        return mask;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public long getLv() {
        return lv;
    }

    public double getDv() {
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
                ", mask=" + mask +
                ", valueType=" + valueType +
                ", lv=" + lv +
                ", dv=" + dv +
                ", preCalculate=" + preCalculate +
                ", calculation=" + calculation +
                '}';
    }


    public static class Calculation {
        private long sum_lv;
        private long max_lv;
        private long min_lv;

        private double sum_dv;
        private double max_dv;
        private double min_dv;


        public Calculation(long sum_lv, long max_lv, long min_lv, double sum_dv, double max_dv, double min_dv) {
            this.sum_lv = sum_lv;
            this.max_lv = max_lv;
            this.min_lv = min_lv;
            this.sum_dv = sum_dv;
            this.max_dv = max_dv;
            this.min_dv = min_dv;
        }


        public long getSum_lv() {
            return sum_lv;
        }

        public long getMax_lv() {
            return max_lv;
        }

        public long getMin_lv() {
            return min_lv;
        }

        public double getSum_dv() {
            return sum_dv;
        }

        public double getMax_dv() {
            return max_dv;
        }

        public double getMin_dv() {
            return min_dv;
        }
    }
}
