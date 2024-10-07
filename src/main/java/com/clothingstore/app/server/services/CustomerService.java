package com.clothingstore.app.server.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.models.Enums.CustomerType;
import com.clothingstore.app.server.models.NewCustomer;
import com.clothingstore.app.server.models.ReturningCustomer;
import com.clothingstore.app.server.models.VIPCustomer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CustomerService {
    private static final String CUSTOMERS_FILE = "src/main/resources/data/customers.json";

    public List<Customer> getAllCustomers() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(CUSTOMERS_FILE);
        
            // Load customers from JSON file
            List<Customer> customers = objectMapper.readValue(file, new TypeReference<List<Customer>>() {});
            return customers;
        } catch (IOException e) {
            throw new RuntimeException("Error loading customers from JSON file", e);
        }
        // Clear existing customers list if necessary
        // customers.clear();
    }


    // public List<Customer> getAllCustomers() {
    //     return customers;
    // }

    // public Optional<Customer> getCustomerById(String customerId) {
        
    //     return customers.stream().filter(c -> c.getCustomerId().equalsIgnoreCase(customerId)).findFirst();
    // }


    public Optional<Customer> getCustomerById(String customerId) {
        List<Customer> customers = getAllCustomers();
        return customers.stream()
                       .filter(customer -> customer.getCustomerId().equals(customerId))
                       .findFirst();
    }

    public List<Customer> getCustomersByType(String customerType) {
        List<Customer> customers = getAllCustomers();
        return customers.stream()
                .filter(customer -> customer.getCustomerType().name().equalsIgnoreCase(customerType))
                .toList();
    }

    public Map<String, Object> getCustomerDetailsMessage(String customerId) {
        Optional<Customer> customerOpt = getCustomerById(customerId);
        System.out.println("customerOpt: " + customerOpt);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            System.out.println("customer: " + customer);
            CustomerType customerType = customer.getCustomerType();
            if(customerType != null) {
                double salePercentage = customer.getDiscountPercentage() * 100;
                System.out.println("salePercentage: " + salePercentage);
                
    
                // Create a map to hold customer details
                Map<String, Object> customerDetails = new HashMap<>();
                customerDetails.put("fullName", customer.getFullName());
                customerDetails.put("customerType", customerType);
                customerDetails.put("salePercentage", salePercentage);
    
                return customerDetails;
            } else {
                throw new RuntimeException("Customer type is not set for customer ID: " + customerId);
            }
        } else {
            throw new RuntimeException("Customer not found.");
        }
    }

    // public void saveCustomer(Customer customer) {
    //     try {
    //         List<Customer> customers = getAllCustomers();
            
    //         ObjectMapper objectMapper = new ObjectMapper();
    //         objectMapper.writeValue(new File(CUSTOMERS_FILE), customers);
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //     }
    // }
}
