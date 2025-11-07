package com.example.assignment.ui.catalog;

import com.example.assignment.data.remote.dto.order.manager.ManagerOrderSummaryDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockDetailDto;
import com.example.assignment.data.remote.dto.stock.AgencyStockItemDto;
import com.example.assignment.data.remote.dto.stock.motorbike.MotorbikeDto;

import java.io.Serializable;

public class VehicleItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long stockId;
    private Long motorbikeId;
    private String name;
    private String model;
    private String version;
    private String imageUrl;
    private double price;
    private int quantity;
    private boolean inStock;
    private ManagerOrderSummaryDto pendingOrder;
    private Long colorId;
    private String colorName;

    public static VehicleItem fromStock(AgencyStockItemDto stock, MotorbikeDto motorbike, AgencyStockDetailDto detail) {
        VehicleItem item = new VehicleItem();
        item.stockId = stock.getId();
        item.motorbikeId = motorbike.getId();
        item.name = motorbike.getName();
        item.model = motorbike.getModel();
        item.version = motorbike.getVersion();
        item.imageUrl = motorbike.getImages() != null && !motorbike.getImages().isEmpty() ? motorbike.getImages().get(0).getImageUrl() : null;
        item.price = stock.getPrice();
        item.quantity = stock.getQuantity();
        item.inStock = stock.getQuantity() > 0;
        if (detail != null && detail.getColor() != null) {
            item.colorId = detail.getColor().getId();
            item.colorName = detail.getColor().getColorType();
        }
        return item;
    }

    public static VehicleItem fromMotorbike(MotorbikeDto motorbike) {
        VehicleItem item = new VehicleItem();
        item.motorbikeId = motorbike.getId();
        item.name = motorbike.getName();
        item.model = motorbike.getModel();
        item.version = motorbike.getVersion();
        item.imageUrl = motorbike.getImages() != null && !motorbike.getImages().isEmpty() ? motorbike.getImages().get(0).getImageUrl() : null;
        item.price = 0;
        item.quantity = 0;
        item.inStock = false;
        return item;
    }

    public Long getStockId() {
        return stockId;
    }

    public Long getMotorbikeId() {
        return motorbikeId;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getVersion() {
        return version;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setPendingOrder(ManagerOrderSummaryDto order) {
        this.pendingOrder = order;
    }

    public ManagerOrderSummaryDto getPendingOrder() {
        return pendingOrder;
    }

    public Long getColorId() {
        return colorId;
    }

    public String getColorName() {
        return colorName;
    }

    public boolean matchesQuery(String query) {
        return (name != null && name.toLowerCase().contains(query))
                || (colorName != null && colorName.toLowerCase().contains(query))
                || (model != null && model.toLowerCase().contains(query))
                || (version != null && version.toLowerCase().contains(query));
    }
}


