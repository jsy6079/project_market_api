package com.project;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
    	
        // 오늘 날짜에서 하루 전 
    	// 테스트 데이터 삽입 시 25년 4월 5일자로 조회하기
        LocalDate yesterday = LocalDate.now().minusDays(1);
        String today = yesterday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        System.out.println("시스템 날짜: " + LocalDate.now());
        System.out.println("조회 날짜: " + today);


        // API 호출
        List<MarketPrice> result = KamisApiFetcher.fetchPrices(200, today);
        result.addAll(KamisApiFetcher.fetchPrices(500, today));
        result.addAll(KamisApiFetcher.fetchPrices(600, today));

        MarketDataInserter.insertPrices(result);
    }
}
