package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;

public class NewCustomer extends Customer {

    public NewCustomer(String customerId, String fullName, String postalCode, String phoneNumber, CustomerType customerType) {
        super(customerId, fullName, postalCode, phoneNumber, customerType);
    }

    public NewCustomer() {
        this.setCustomerType(CustomerType.NEW);
    }

    @Override
    public String handlePurchase() {
        return "Handled by New Customers Department - Purchase initiated for: " + this.getFullName();
    }
}
