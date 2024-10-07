package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VIPCustomer extends Customer {
    // private static final int POINTS_CONVERSION_RATE = 10; // 10 shekels for 100 points
    public VIPCustomer() {
        super();
        // setDiscountPercentage(0.3);
    }
    // public VIPCustomer(String customerId, String fullName, String postalCode, String phoneNumber) {
    //     super(customerId, fullName, postalCode, phoneNumber, CustomerType.VIP, 0.3);
    // }
    @JsonCreator
    public VIPCustomer(
        @JsonProperty("customerId") String customerId,
        @JsonProperty("fullName") String fullName,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("phoneNumber") String phoneNumber
    ) {
        super(customerId, fullName, postalCode, phoneNumber, CustomerType.VIP);
    }
    @Override
    public double getDiscountPercentage() {
        return 0.3;
    }

    // @Override
    // public double convertPointsToCurrency() {
    //     return getPoints() / 100 * POINTS_CONVERSION_RATE;
    // }
}
