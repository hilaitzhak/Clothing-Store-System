package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewCustomer extends Customer {


    public NewCustomer() {
        super();
    }

    @JsonCreator
    public NewCustomer(
        @JsonProperty("customerId") String customerId,
        @JsonProperty("fullName") String fullName,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("phoneNumber") String phoneNumber
    ) {
        super(customerId, fullName, postalCode, phoneNumber, CustomerType.NEW);
    }

    @Override
    public double getDiscountPercentage() {
        return 0.1;
    }
}
