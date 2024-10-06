package com.clothingstore.app.server.controllers;

import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        System.out.println("Fetching products for branchId: " + branchId);
        return productService.getProductsByBranch(branchId);
    }

    // Handle buying a product
    @PostMapping("/buy")
    public ResponseEntity<String> buyProduct(@RequestParam String productId, @RequestParam int quantity) {
        try {
            productService.buyProduct(productId, quantity);
            return ResponseEntity.ok("Purchase successful!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    // Handle selling a product
    @PostMapping("/sell")
    public boolean sellProduct(@RequestParam String productId, @RequestParam int quantity) {
        return productService.sellProduct(productId, quantity);
    }
}
