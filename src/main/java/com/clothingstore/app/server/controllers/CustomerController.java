package com.clothingstore.app.server.controllers;

import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public Customer getCustomerById(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @PostMapping("/{customerId}/purchase")
    public String initiatePurchase(@PathVariable String customerId) {
        return customerService.handlePurchase(customerId);
    }
}
