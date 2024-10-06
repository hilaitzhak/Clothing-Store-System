package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;

public class ReturningCustomer extends Customer {

    public ReturningCustomer(String customerId, String fullName, String postalCode, String phoneNumber, CustomerType customerType) {
        super(customerId, fullName, postalCode, phoneNumber, customerType);
    }
    public ReturningCustomer() {
        this.setCustomerType(CustomerType.RETURNING);
    }

    @Override
    public String handlePurchase() {
        return "Handled by Returning Customers Department - Purchase initiated for: " + this.getFullName();
    }
}
