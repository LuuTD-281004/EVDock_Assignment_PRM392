package com.example.assignment.data.remote.dto.stock.promotion;

import com.google.gson.annotations.SerializedName;

public class StockPromotionWrapperDto {

    @SerializedName("stockPromotionId")
    private long stockPromotionId;

    @SerializedName("stockPromotion")
    private StockPromotionDto stockPromotion;

    public long getStockPromotionId() {
        return stockPromotionId;
    }

    public StockPromotionDto getStockPromotion() {
        return stockPromotion;
    }
}


