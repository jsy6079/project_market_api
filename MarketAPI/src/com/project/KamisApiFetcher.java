package com.project;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import org.json.JSONObject;
import org.json.JSONArray;

public class KamisApiFetcher {

    // 조건 필터용 클래스
    static class TargetItem {
        String itemCode;
        String kindName;
        String rank;

        public TargetItem(String itemCode, String kindName, String rank) {
            this.itemCode = itemCode;
            this.kindName = kindName;
            this.rank = rank;
        }

        public boolean matches(JSONObject item) {
            return itemCode.equals(item.getString("item_code"))
                && kindName.equals(item.getString("kind_name"))
                && rank.equals(item.getString("rank"));
        }
    }

    private static final List<TargetItem> targetItems = Arrays.asList(
        new TargetItem("211", "월동(1포기)", "상품"),    
        new TargetItem("213", "시금치(100g)", "상품"),   
        new TargetItem("245", "양파(1kg)", "상품"),
        new TargetItem("221", "수박(1개)", "상품"), 
        new TargetItem("226", "딸기(100g)", "상품"), 
        new TargetItem("4304", "갈비", "갈비"), 
        new TargetItem("4301", "갈비", "1등급"), 
        new TargetItem("9908", "흰우유", "흰우유"), 
        new TargetItem("611", "국산(냉동)(1마리)", "大"), 
        new TargetItem("613", "국산(냉동)(1마리)", "大"),
        new TargetItem("644", "굴(1kg)", "상품")
    );

    public static List<MarketPrice> fetchPrices(int itemCategoryCode, String regday) {
        List<MarketPrice> collected = new ArrayList<>();

        try {
            String baseUrl = "https://www.kamis.or.kr/service/price/xml.do";
            String params = String.format(
                "?action=dailyPriceByCategoryList" +
                "&p_cert_key=3b419e6c-6c57-4a81-9486-a3acf0c6d8a5" +
                "&p_cert_id=5462" +
                "&p_returntype=json" +
                "&p_product_cls_code=01" +
                "&p_item_category_code=%d" +
                "&p_country_code=1101" +
                "&p_regday=%s" +
                "&p_convert_kg_yn=n", itemCategoryCode, regday);

            URL url = new URL(baseUrl + params);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");

            BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), "UTF-8")
            );

            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();

            JSONObject json = new JSONObject(sb.toString());

            if (json.has("data")) {
                JSONObject data = json.getJSONObject("data");
                if (data.has("item")) {
                    JSONArray items = data.getJSONArray("item");

                    for (int i = 0; i < items.length(); i++) {
                        JSONObject item = items.getJSONObject(i);
                        for (TargetItem target : targetItems) {
                            if (target.matches(item)) {
                                String itemName = item.getString("item_name");
                                String price = item.getString("dpr1");
                                
                                System.out.println("상품 : "+ itemName +"가격 :" + price );
                                collected.add(new MarketPrice(itemName, price));
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return collected;
    }
}
