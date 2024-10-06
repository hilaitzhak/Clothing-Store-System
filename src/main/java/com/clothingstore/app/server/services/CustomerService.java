package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {

    private List<Customer> customers = new ArrayList<>(); // Initialize the customers list

    public CustomerService() {
        try {
            loadCustomers();
        } catch (IOException e) {
            throw new RuntimeException("Error loading customers from JSON file", e);
        }
    }

    private void loadCustomers() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("src/main/java/com/clothingstore/app/server/data/customers.json");

        // Read the data from JSON file
        List<Customer> tempCustomers = objectMapper.readValue(file, new TypeReference<List<Customer>>() {});

        // Populate the correct customer types based on customerType
        for (Customer tempCustomer : tempCustomers) {
            switch (tempCustomer.getCustomerType()) {
                case NEW:
                    customers.add(new NewCustomer(
                        tempCustomer.getCustomerId(),
                        tempCustomer.getFullName(),
                        tempCustomer.getPostalCode(),
                        tempCustomer.getPhoneNumber(),
                        tempCustomer.getCustomerType()
                    ));
                    break;
                case RETURNING:
                    customers.add(new ReturningCustomer(
                        tempCustomer.getCustomerId(),
                        tempCustomer.getFullName(),
                        tempCustomer.getPostalCode(),
                        tempCustomer.getPhoneNumber(),
                        tempCustomer.getCustomerType()
                    ));
                    break;
                case VIP:
                    customers.add(new VIPCustomer(
                        tempCustomer.getCustomerId(),
                        tempCustomer.getFullName(),
                        tempCustomer.getPostalCode(),
                        tempCustomer.getPhoneNumber(),
                        tempCustomer.getCustomerType()
                    ));
                    break;
                default:
                    throw new IllegalArgumentException("Unknown customer type: " + tempCustomer.getCustomerType());
            }
        }
    }

    public List<Customer> getAllCustomers() {
        return customers;
    }

    public Optional<Customer> getCustomerById(String customerId) {
        return customers.stream().filter(c -> c.getCustomerId().equals(customerId)).findFirst();
    }

    public List<Customer> getCustomersByType(String customerType) {
        return customers.stream()
                .filter(customer -> customer.getCustomerType().name().equalsIgnoreCase(customerType))
                .toList();
    }

    public String handlePurchase(String customerId) {
        Optional<Customer> customerOpt = getCustomerById(customerId);
        if (customerOpt.isPresent()) {
            Customer customer = customerOpt.get();
            return customer.handlePurchase();
        } else {
            return "Customer not found.";
        }
    }
}
