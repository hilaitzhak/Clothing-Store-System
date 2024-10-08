package com.clothingstore.app.server.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.clothingstore.app.server.models.User;
import com.clothingstore.app.server.services.UserService;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @GetMapping
    public List<User> getAllUsers(@RequestParam String branchId) {
        return userService.getUsersByBranchId(branchId);
    }

    @GetMapping("/{username}/details")
    public Map<String, Object> getUserDetails(@PathVariable String username) {
        return userService.getUserDetailsMessage(username);
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        boolean loginSuccess = userService.login(username, password);

        if (loginSuccess) {
            return "Login successful!";
        } else {
            return "Invalid username or password.";
        }
    }
}
