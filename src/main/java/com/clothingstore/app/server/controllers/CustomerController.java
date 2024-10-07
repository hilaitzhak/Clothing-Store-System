package com.clothingstore.app.server.controllers;

import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping("/all")
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/{customerId}")
    public Customer getCustomerById(@PathVariable String customerId) {
        return customerService.getCustomerById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
    }

    @GetMapping("/{customerId}/details")
    public Map<String, Object> getCustomerDetails(@PathVariable String customerId) {
        return customerService.getCustomerDetailsMessage(customerId);
    }

    
}
