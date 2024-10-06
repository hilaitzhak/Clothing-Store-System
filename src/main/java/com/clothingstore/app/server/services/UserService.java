package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {

    private List<User> users;

    public UserService() {
        try {
            loadUsers();
        } catch (IOException e) {
            throw new RuntimeException("Error loading users from JSON file", e);
        }
    }

    private void loadUsers() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        File file = new File("src/main/java/com/clothingstore/app/server/data/users.json");
        users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
    }

    // Get all users with a specific branch
    public List<User> getUsersByBranchId(String branchId) {
        List<User> relevantUsers = new ArrayList<>();
        for (User user : users) {
            if (user.getBranchId().equals(branchId)) {
            relevantUsers.add(user);
            }
        }
        return relevantUsers;
    }

    // New login method to check credentials
    public boolean login(String username, String password) {
        return users.stream()
                .anyMatch(user -> user.getUsername().equals(username) && user.getPassword().equals(password));
    }
}
