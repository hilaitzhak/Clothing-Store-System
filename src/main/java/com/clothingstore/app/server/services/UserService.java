package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private static final String USERS_FILE = "src/main/resources/data/users.json";
    private List<User> users = new ArrayList<>();

    public UserService() {
        try {
            loadUsers();
        } catch (IOException e) {
            throw new RuntimeException("Error loading users from JSON file", e);
        }
    }

    private void loadUsers() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File(USERS_FILE);
        users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
    }

    // Get all users with a specific branch
    // public List<User> getUsersByBranchId(String branchId) {
    //     List<User> relevantUsers = new ArrayList<>();
    //     for (User user : users) {
    //         System.out.println("User: " + user);  // Print each user to verify their details
    //         if (user.getBranchId().equalsIgnoreCase(branchId)) {
    //             relevantUsers.add(user);
    //         }
    //     }
    //     return relevantUsers;
    // }
    

    public List<User> getUsersByBranchId(String branchId) {
    return users.stream()
            .filter(user -> user.getBranchId().equals(branchId))
            .collect(Collectors.toList());
    }

    // New login method to check credentials
    public boolean login(String username, String password) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));
    }

    public Map<String, Object> getUserDetailsMessage(String username) {
        Optional<User> userOpt = getUserByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Create a map to hold customer details
            Map<String, Object> userDetails = new HashMap<>();
            userDetails.put("fullName", user.getFullName());
            userDetails.put("role", user.getRole());
            userDetails.put("branchId", user.getBranchId());
            return userDetails;
        } else {
            throw new RuntimeException("Customer not found.");
        }
    }

    public boolean isShiftManager(String username) {
        Optional<User> userOpt = getUserByUsername(username);
        return userOpt.map(User::isShiftManager).orElse(false);
    }

    public Optional<User> getUserByUsername(String username) {
        return users.stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }
}
