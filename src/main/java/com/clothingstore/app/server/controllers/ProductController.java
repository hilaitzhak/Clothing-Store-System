package com.clothingstore.app.server.controllers;

import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;


    // Get products for the user's branch
    @GetMapping
    public List<Product> getProductsByBranch(@RequestParam String branchId) {
        if (branchId == null || branchId.isEmpty()) {
            throw new IllegalArgumentException("Branch ID must not be null or empty");
        }
        return productService.getProductsByBranch(branchId);
    }

    // Handle buying a product
    @PostMapping("/buy")
    public ResponseEntity<String> buyProduct(@RequestParam String productId, @RequestParam int quantity, @RequestParam String customerId) {
        try {
            productService.buyProduct(productId, quantity, customerId);
            return ResponseEntity.ok("Purchase successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    // Handle selling a product
    @PostMapping("/sell")
    public ResponseEntity<String> sellProduct(@RequestParam String productId, @RequestParam int quantity, @RequestParam String customerId) {
        try {
            productService.sellProduct(productId, quantity, customerId);
            return ResponseEntity.ok("Sell successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
