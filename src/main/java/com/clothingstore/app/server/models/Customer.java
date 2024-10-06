package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;

public class Customer {
    private String customerId;
    private String fullName;
    private String postalCode;
    private String phoneNumber;
    private String email;
    private CustomerType customerType;

    public Customer() {
    }

    public Customer(String customerId, String fullName, String postalCode, String phoneNumber, CustomerType customerType) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.customerType = customerType;
    }

    public String getCustomerId() { return customerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPostalCode() { return postalCode; }
    public void setPostalCode(String postalCode) { this.postalCode = postalCode; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public CustomerType getCustomerType() { return customerType; }
    public void setCustomerType(CustomerType customerType) { this.customerType = customerType; }

    // This method can be overridden by subclasses
    public String handlePurchase() {
        return "Handling general purchase for customer: " + fullName;
    }
}
