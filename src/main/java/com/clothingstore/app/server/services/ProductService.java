package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.models.Product;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductService {
    private static final String PRODUCTS_FILE = "src/main/resources/data/products.json";
    private List<Product> products;
    
    @Autowired
    private CustomerService customerService;

    public ProductService() {
        try {
            loadProducts();
        } catch (IOException e) {
            throw new RuntimeException("Error loading customers from JSON file", e);
        }
    }

    // Load products from the JSON file
    private void loadProducts() throws IOException{
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            File file = new File(PRODUCTS_FILE);
            if (file.exists()) {
                products = objectMapper.readValue(file, new TypeReference<List<Product>>() {});
            } else {
                products = List.of(); // Empty list if the file doesn't exist
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save products back to the JSON file
    private void saveProducts() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(PRODUCTS_FILE), products);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all products for a specific branch
    public List<Product> getProductsByBranch(String branchId) {
        List<Product> availableProducts = new ArrayList<>();
        for (Product product : products) {
            if (product.getBranchIds().contains(branchId)) { // Check if product's branchId list contains user's branchId
                availableProducts.add(product);
            }
        }
        return availableProducts;
    }

    // Buy a product (decrease stock)
    public void buyProduct(String productId, int quantity, String customerId) {
        Optional<Product> optionalProduct = findProductById(productId);
        Optional<Customer> optionalCustomer = customerService.getCustomerById(customerId);

        if (optionalProduct.isPresent() && optionalCustomer.isPresent()) {
            Product product = optionalProduct.get();
            
            // Check if the stock is enough for the purchase
            if (product.getStockQuantity() >= quantity) {
                // Deduct the stock
                product.setStockQuantity(product.getStockQuantity() - quantity);

                saveProducts();
            } else {
                throw new IllegalArgumentException("Not enough stock. Only " + product.getStockQuantity() + " items available.");
            }
        } else {
            throw new IllegalArgumentException("Product or Customer not found.");
        }
    }


    // Sell a product (increase stock)
    public void sellProduct(String productId, int quantity, String customerId) {
        Optional<Product> optionalProduct = findProductById(productId);
        Optional<Customer> optionalCustomer = customerService.getCustomerById(customerId);

        if (optionalProduct.isPresent() && optionalCustomer.isPresent()) {
            Product product = optionalProduct.get();

            // Increase stock
            product.setStockQuantity(product.getStockQuantity() + quantity);

            saveProducts();
        } else {
            throw new IllegalArgumentException("Product or Customer not found.");
        }
    }

    // Find product by ID
    private Optional<Product> findProductById(String productId) {
        return products.stream()
                .filter(product -> product.getProductId().equals(productId))
                .findFirst();
    }
}
