package com.clothingstore.app.server.models;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Enums {
    public enum UserRole {
        SHIFT_MANAGER, CASHIER, SELLER, ADMIN
    }

    public enum CustomerType {
        NEW, RETURNING, VIP;
    
    }


}
