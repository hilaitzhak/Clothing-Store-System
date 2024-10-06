package com.clothingstore.app.server.models;

import java.util.List;

public class Product {
    private String productId;
    private String productName;
    private double price;
    private int stockQuantity;
    private List<String> branchIds;

    public Product() {}

    public Product(String productId, String productName, int stockQuantity, double price, List<String> branchIds) {
        this.productId = productId;
        this.productName = productName;
        this.stockQuantity = stockQuantity;
        this.price = price;
        this.branchIds = branchIds;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public List<String> getBranchIds() {
        return branchIds;
    }

    public void setBranchIds(List<String> branchIds) {
        this.branchIds = branchIds;
    }
}
