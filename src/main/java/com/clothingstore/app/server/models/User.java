package com.clothingstore.app.server.models;

import com.clothingstore.app.server.models.Enums.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    private String userId;
    private String username;
    private String password;
    private String fullName;
    private String accountNumber;
    private String employeeNumber;
    private String phoneNumber;
    private UserRole role;
    private String branchId;

     // Default constructor
     public User() {
    }

    public User(String id, String name, String password) {
        this.userId = id;
        this.username = name;
        this.password = password;
    }

    public User(String userId, String fullName, String accountNumber, String employeeNumber, UserRole role, String branchId, String phoneNumber) {
        this.userId = userId;
        this.fullName = fullName;
        this.accountNumber = accountNumber;
        this.employeeNumber = employeeNumber;
        this.role = role;
        this.branchId = branchId;
        this.phoneNumber = phoneNumber;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String id) {
        this.userId = id;
    }

    @JsonProperty("username")
    public String getUsername() {
        return username;
    }

    public void setUsername(String name) {
        this.username = name;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public void setEmployeeNumber(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }
}
