package com.clothingstore.app.server.models;

// import org.springframework.beans.factory.annotation.Autowired;

import com.clothingstore.app.server.models.Enums.CustomerType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NewCustomer extends Customer {

    // private static final int POINTS_CONVERSION_RATE = 1; // 1 shekel for 100 points

    public NewCustomer() {
        super();
    }
    // public NewCustomer(String customerId, String fullName, String postalCode, String phoneNumber) {
    //     super(customerId, fullName, postalCode, phoneNumber, CustomerType.NEW,0.1);
    // }
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

    // @Override
    // public double convertPointsToCurrency() {
    //     return getPoints() / 100 * POINTS_CONVERSION_RATE;
    // }
}
