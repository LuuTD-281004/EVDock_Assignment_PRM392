package com.example.assignment.ui.stock.model;

public class StockListItem {
    private final long id;
    private final String motorbikeName;
    private final String model;
    private final String version;
    private final String imageUrl;
    private final int quantity;
    private final double price;

    public StockListItem(long id,
                         String motorbikeName,
                         String model,
                         String version,
                         String imageUrl,
                         int quantity,
                         double price) {
        this.id = id;
        this.motorbikeName = motorbikeName;
        this.model = model;
        this.version = version;
        this.imageUrl = imageUrl;
        this.quantity = quantity;
        this.price = price;
    }

    public long getId() {
        return id;
    }

    public String getMotorbikeName() {
        return motorbikeName;
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

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}


