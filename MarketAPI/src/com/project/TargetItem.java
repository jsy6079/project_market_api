package com.project;

import org.json.JSONObject;

// OpenAPI 필터링 할 객체 생성
public class TargetItem {
	
	public String itemCode;
	public String kindName;
	public String rank ;
	
    public TargetItem(String itemCode, String kindName, String rank) {
        this.itemCode = itemCode;
        this.kindName = kindName;
        this.rank  = rank ;
    }

    public boolean matches(JSONObject item) {
        return itemCode.equals(item.optString("item_code")) &&
        		kindName.equals(item.optString("kind_name")) &&
               rank.equals(item.optString("rank"));
    }
	

}
