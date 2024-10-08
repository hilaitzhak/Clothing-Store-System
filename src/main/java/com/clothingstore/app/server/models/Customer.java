package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "customerType" // Or another field that determines the type
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = NewCustomer.class, name = "NEW"),
    @JsonSubTypes.Type(value = ReturningCustomer.class, name = "RETURNING"),
    @JsonSubTypes.Type(value = VIPCustomer.class, name = "VIP")
})
public abstract class Customer {
    private String customerId;
    private String fullName;
    private String postalCode;
    private String phoneNumber;
    private CustomerType customerType;

    public Customer() {
    }

    public Customer(
        String customerId,
        String fullName,
        String postalCode,
        String phoneNumber,
        CustomerType customerType
    ) {
        this.customerId = customerId;
        this.fullName = fullName;
        this.postalCode = postalCode;
        this.phoneNumber = phoneNumber;
        this.customerType = customerType;
    }

    // Getters and Setters
    public String getCustomerId() {
        return customerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public CustomerType getCustomerType() {
        return customerType;
    }

    public void setCustomerType(CustomerType customerType) {
        this.customerType = customerType;
    }


        // public double getDiscountPercentage() {
        //     return discountPercentage;
        // }

        // public void setDiscountPercentage(double discountPercentage) {
        //     this.discountPercentage = discountPercentage;
        // }
        // Abstract method to be implemented by subclasses
        // public abstract double convertPointsToCurrency();
        
        public abstract double getDiscountPercentage();
    }