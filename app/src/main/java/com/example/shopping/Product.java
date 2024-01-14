package com.example.shopping;

import android.os.Parcel;
import android.os.Parcelable;

public class Product {
    private String productId;
    private String productName;
    private String productPrice;
    private String productImage;
    private int quantity;

    public Product(String productId, String productName, String productPrice, String productImage, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productImage = productImage;
        this.quantity = quantity;
    }

    public void decreaseQuantity() {
        this.quantity--;
    }

    public int getQuantity() {
        return quantity;
    }

    public void increaseQuantity() {
        this.quantity++;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getProductPrice() {
        return productPrice;
    }

    public String getProductImage() {
        return productImage;
    }
}
