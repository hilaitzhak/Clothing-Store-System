package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;

public class VIPCustomer extends Customer {

    
    public VIPCustomer(String customerId, String fullName, String postalCode, String phoneNumber, CustomerType customerType) {
        super(customerId, fullName, postalCode, phoneNumber, customerType);
    }
    public VIPCustomer () {
        this.setCustomerType(CustomerType.VIP);
    }

    @Override
    public String handlePurchase() {
        return "Handled by VIP Department - Purchase initiated for: " + this.getFullName();
    }
}
