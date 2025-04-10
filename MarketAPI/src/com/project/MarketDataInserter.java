package com.project;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MarketDataInserter {

    private static final Map<String, Long> productIdMap = new HashMap<>();
    static {
        productIdMap.put("배추", 1L);
        productIdMap.put("시금치", 2L);
        productIdMap.put("양파", 3L);
        productIdMap.put("수박", 4L);
        productIdMap.put("딸기", 5L);
        productIdMap.put("돼지", 6L);
        productIdMap.put("소", 7L);
        productIdMap.put("우유", 8L);
        productIdMap.put("고등어", 9L);
        productIdMap.put("갈치", 10L);
        productIdMap.put("굴", 11L);
    }

    public static void insertPrices(List<MarketPrice> prices) throws Exception {
        Connection conn = DBUtil.getConnection();
        
     // 모든 storeId, productId 조합을 가져옴
        String pairQuery = "SELECT storeId, productId FROM ProductPrice GROUP BY storeId, productId";
        PreparedStatement pairStmt = conn.prepareStatement(pairQuery);
        ResultSet pairRs = pairStmt.executeQuery();

        while (pairRs.next()) {
            long storeId = pairRs.getLong("storeId");
            long productId = pairRs.getLong("productId");

            // 해당 조합의 등록일 기준 정렬된 id 목록을 가져옴
            String priceQuery = "SELECT productPriceId FROM ProductPrice WHERE storeId = ? AND productId = ? ORDER BY productRegDate ASC";
            PreparedStatement priceStmt = conn.prepareStatement(priceQuery);
            priceStmt.setLong(1, storeId);
            priceStmt.setLong(2, productId);
            ResultSet priceRs = priceStmt.executeQuery();

            List<Long> ids = new ArrayList<>();
            while (priceRs.next()) {
                ids.add(priceRs.getLong("productPriceId"));
            }
            priceRs.close();
            priceStmt.close();

            // 5개 초과 시 오래된 것부터 삭제
            if (ids.size() > 5) {
                int numToDelete = ids.size() - 5;
                List<Long> toDelete = ids.subList(0, numToDelete);

                String deleteQuery = "DELETE FROM ProductPrice WHERE productPriceId = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery);
                
                for (Long id : toDelete) {
                    deleteStmt.setLong(1, id);
                    int deleted = deleteStmt.executeUpdate();
                    if (deleted > 0) {
                        System.out.println("삭제된 ProductPrice ID: " + id + " (storeId: " + storeId + ", productId: " + productId + ")");
                    }
                }

                deleteStmt.close();
            }
        }

        pairRs.close();
        pairStmt.close();

        
        

        // storeId -> marketId 매핑
        Map<Long, Long> storeToMarket = new HashMap<>();
        ResultSet rs1 = conn.createStatement().executeQuery("SELECT storeId, marketId FROM Store");
        while (rs1.next()) {
            storeToMarket.put(rs1.getLong("storeId"), rs1.getLong("marketId"));
        }
        rs1.close();

        // marketId -> store 수 매핑
        Map<Long, Integer> marketStoreCount = new HashMap<>();
        ResultSet rs2 = conn.createStatement().executeQuery("SELECT marketId, COUNT(*) as count FROM Store GROUP BY marketId");
        while (rs2.next()) {
            marketStoreCount.put(rs2.getLong("marketId"), rs2.getInt("count"));
        }
        rs2.close();

        // storeId -> 취급 상품 (store.productCategoryId 와 product.productCategoryId 매칭)
        Map<Long, List<Long>> storeProductMap = new HashMap<>();
        ResultSet rs3 = conn.createStatement().executeQuery(
            "SELECT s.storeId, p.productId FROM Store s JOIN Product p ON s.productCategoryId = p.productCategoryId"
        );
        while (rs3.next()) {
            long storeId = rs3.getLong("storeId");
            long productId = rs3.getLong("productId");
            storeProductMap.computeIfAbsent(storeId, k -> new ArrayList<>()).add(productId);
        }
        rs3.close();

        String sql = "INSERT INTO ProductPrice (productPriceCost, productRegDate, productId, storeId) VALUES (?, NOW(), ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);

        for (MarketPrice price : prices) {
            Long productId = productIdMap.get(price.itemName);
            if (productId == null) continue;
            
            if (price.price == null || price.price.trim().equals("-")) continue;

            long baseCost = Long.parseLong(price.price.replace(",", ""));

            for (Map.Entry<Long, List<Long>> entry : storeProductMap.entrySet()) {
                long storeId = entry.getKey();
                List<Long> allowedProducts = entry.getValue();

                if (allowedProducts.contains(productId)) {
                    long adjustedCost = adjustPrice(baseCost, storeId, storeToMarket, marketStoreCount);

                    ps.setLong(1, adjustedCost);
                    ps.setLong(2, productId);
                    ps.setLong(3, storeId);
                    ps.addBatch();
                }
            }
        }

        int[] results = ps.executeBatch();
        System.out.println("추가된 행 수: " + results.length);

        ps.close();
        conn.close();
    }
   
    private static long adjustPrice(long basePrice, long storeId, Map<Long, Long> storeToMarket, Map<Long, Integer> marketStoreCount) {
        Long marketId = storeToMarket.get(storeId);
        int storeCount = marketStoreCount.getOrDefault(marketId, 1);

        // 점포 수에 따른 퍼센트 지정
        double percent;
        if (storeCount == 3) {
            percent = 0.25; 
        } else if (storeCount == 4) {
            percent = 0.15;
        } else if (storeCount == 5) {
            percent = 0.10;
        } else {
            percent = 0.05;
        }

        double min = 1.0 - percent;
        double max = 1.0 + percent;
        double multiplier = min + Math.random() * (max - min);

        long rawPrice = Math.round(basePrice * multiplier);

        return (rawPrice / 100) * 100;
    }

}
