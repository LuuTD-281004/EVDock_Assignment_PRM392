package com.example.assignment.data.remote.dto.stock;

import java.util.List;

import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;
import com.example.assignment.data.remote.dto.stock.promotion.StockPromotionWrapperDto;
import com.google.gson.annotations.SerializedName;

public class AgencyStockDetailDto {

    @SerializedName("id")
    private long id;

    @SerializedName("motorbikeId")
    private long motorbikeId;

    @SerializedName("agencyId")
    private long agencyId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("price")
    private double price;

    @SerializedName("createAt")
    private String createAt;

    @SerializedName("updateAt")
    private String updateAt;

    @SerializedName("motorbike")
    private MotorbikeDto motorbike;

    @SerializedName("color")
    private ColorDto color;

    @SerializedName("agencyStockPromotion")
    private List<StockPromotionWrapperDto> promotions;

    public long getId() {
        return id;
    }

    public long getMotorbikeId() {
        return motorbikeId;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public String getCreateAt() {
        return createAt;
    }

    public String getUpdateAt() {
        return updateAt;
    }

    public MotorbikeDto getMotorbike() {
        return motorbike;
    }

    public ColorDto getColor() {
        return color;
    }

    public List<StockPromotionWrapperDto> getPromotions() {
        return promotions;
    }

    public static class ColorDto {
        @SerializedName("id")
        private long id;

        @SerializedName("colorType")
        private String colorType;

        public long getId() {
            return id;
        }

        public String getColorType() {
            return colorType;
        }
    }
}


