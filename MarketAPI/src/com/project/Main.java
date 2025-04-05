package com.project;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
    	
        // 오늘 날짜에서 하루 전 -> 안전하게 2일전으로 직전에 가격이안나오는경우가있음
        LocalDate yesterday = LocalDate.now().minusDays(2);
        String today = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println("조회 날짜: " + today);

        // API 호출
        List<MarketPrice> result = KamisApiFetcher.fetchPrices(200, today);
        result.addAll(KamisApiFetcher.fetchPrices(500, today));
        result.addAll(KamisApiFetcher.fetchPrices(600, today));

        MarketDataInserter.insertPrices(result);
    }
}
