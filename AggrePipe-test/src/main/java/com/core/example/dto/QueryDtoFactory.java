package com.core.example.dto;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class QueryDtoFactory {



    public static QueryDtoFactory.statistics getStatistics(String serialNumber, int number, int percentage, int range) {
        List<QueryDto> list = new ArrayList<>();
        Random rd = new Random();

        double d = (number / 100);
        int size = (int) (d * percentage);


        long same_userId = Math.abs(rd.nextInt(range));
        long same_orderId = Math.abs(rd.nextInt(range));

        for(int i=0; i<size; i++) {
            double unitPrice = rd.nextDouble() * range;
            long quantity = Math.abs(rd.nextInt(range));
            QueryDto queryDto = new QueryDto(same_userId, same_orderId, unitPrice, quantity, LocalDateTime.now());
            list.add(queryDto);
        }

        for(int i=size; i<number; i++) {
            long userId = Math.abs(rd.nextInt(range));
            long orderId = Math.abs(rd.nextInt(range));
            double unitPrice = rd.nextDouble() * range;
            long quantity = Math.abs(rd.nextInt(range));
            QueryDto queryDto = new QueryDto(userId, orderId, unitPrice, quantity, LocalDateTime.now());
            list.add(queryDto);
        }

        // 키로 그루핑
        Map<String, List<QueryDto>> collect = list.stream().collect(Collectors.groupingBy(queryDto -> generateKey(serialNumber, queryDto.getUserId(), queryDto.getOrderId())));

        Map<String, Long> counts = new HashMap<>();

        Map<String, staticCal> data = new HashMap<>();


        for (var e : collect.entrySet()) {
            List<QueryDto> queryDtos = e.getValue();
            QueryDto base = queryDtos.get(0);
            String key = e.getKey();

            double sum_unitPrice = base.getUnitPrice();
            double max_unitPrice = base.getUnitPrice();
            double min_unitPrice = base.getUnitPrice();

            long max_quantity = base.getQuantity();
            long min_quantity = base.getQuantity();

            for(int i=1; i<queryDtos.size(); i++) {
                QueryDto unit = queryDtos.get(i);
                Double unitPrice = unit.getUnitPrice();
                Long quantity = unit.getQuantity();

                sum_unitPrice += unitPrice;
                max_unitPrice = Math.max(max_unitPrice, unitPrice);
                min_unitPrice = Math.min(min_unitPrice, unitPrice);

                max_quantity = Math.max(max_quantity, quantity);
                min_quantity = Math.min(min_quantity, quantity);
            }
            counts.put(key, (long) queryDtos.size());
            staticCal cal = new staticCal(sum_unitPrice, max_unitPrice, min_unitPrice, max_quantity, min_quantity);
            data.put(key, cal);
        }

        return new statistics(list, counts, data);
    }
    private static String generateKey(String SERIAL_NUMBER, long userId, long orderId) {
        return "["+SERIAL_NUMBER +"]" + userId + "," + orderId;
    }




    public static class statistics {
        private List<QueryDto> queryDto;

        private Map<String, Long> counts;

        private Map<String, staticCal> data;


        public statistics(List<QueryDto> queryDto, Map<String, Long> counts, Map<String, staticCal> data) {
            this.queryDto = queryDto;
            this.counts = counts;
            this.data = data;
        }

        public List<QueryDto> getQueryDto() {
            return queryDto;
        }

        public Map<String, Long> getCounts() {
            return counts;
        }

        public Map<String, staticCal> getData() {
            return data;
        }
    }

    public static class staticCal {

        public double sum_unitPrice;
        public double max_unitPrice;
        public double min_unitPrice;

        public long max_quantity;
        public long min_quantity;

        public staticCal(double sum_unitPrice, double max_unitPrice, double min_unitPrice, long max_quantity, long min_quantity) {
            this.sum_unitPrice = sum_unitPrice;
            this.max_unitPrice = max_unitPrice;
            this.min_unitPrice = min_unitPrice;
            this.max_quantity = max_quantity;
            this.min_quantity = min_quantity;
        }

    }
}
