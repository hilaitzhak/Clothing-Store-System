package com.clothingstore.app.server.models;

// import org.springframework.beans.factory.annotation.Autowired;

import com.clothingstore.app.server.models.Enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReturningCustomer extends Customer {

    // private static final int POINTS_CONVERSION_RATE = 5; // 5 shekels for 100 points
    public ReturningCustomer() {
        super();
        // setDiscountPercentage(0.2);
    }
    // public ReturningCustomer(String customerId, String fullName, String postalCode, String phoneNumber) {
    //     super(customerId, fullName, postalCode, phoneNumber, CustomerType.RETURNING,0.2);
    // }
    @JsonCreator
    public ReturningCustomer(
        @JsonProperty("customerId") String customerId,
        @JsonProperty("fullName") String fullName,
        @JsonProperty("postalCode") String postalCode,
        @JsonProperty("phoneNumber") String phoneNumber
    ) {
        super(customerId, fullName, postalCode, phoneNumber, CustomerType.RETURNING);
    }
    @Override
    public double getDiscountPercentage() {
        return 0.2;
    }

    // @Override
    // public double convertPointsToCurrency() {
    //     return getPoints() / 100 * POINTS_CONVERSION_RATE;
    // }
}
