package com.clothingstore.app.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.models.User;

public class Client {
    // ANSI escape codes for colors
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String BLUE = "\033[0;34m";
    private static final String YELLOW = "\033[0;33m";
    private static final String CYAN = "\033[0;36m";

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static String userBranchId;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        boolean loggedIn = false;
        while (!loggedIn) {
            loggedIn = attemptLogin(scanner);
            if (!loggedIn) {
                System.out.println(YELLOW + "Press Enter to try again or type 'exit' to quit: " + RESET);
                String input = scanner.nextLine();
                if ("exit".equalsIgnoreCase(input.trim())) {
                    System.out.println(RED + "Exiting program." + RESET);
                    scanner.close();
                    return;
                }
            }
        }

        runMainMenu(scanner);
        scanner.close();
    }

    private static boolean attemptLogin(Scanner scanner) {
        for (int attempt = 1; attempt <= MAX_LOGIN_ATTEMPTS; attempt++) {
            System.out.println(CYAN + "Login attempt " + attempt + " of " + MAX_LOGIN_ATTEMPTS + RESET);
            if (handleLogin(scanner)) {
                return true;
            }
            if (attempt < MAX_LOGIN_ATTEMPTS) {
                System.out.println(YELLOW + "Press Enter to try again..." + RESET);
                scanner.nextLine();
            }
        }
        System.out.println(RED + "You've reached the maximum number of login attempts." + RESET);
        return false;
    }

    private static boolean handleLogin(Scanner scanner) {
        System.out.println(CYAN + "Please enter username: " + RESET);
        String username = scanner.nextLine();
    
        System.out.println(CYAN + "Please enter password: " + RESET);
        String password = scanner.nextLine();
    
        try {
            // Check if the login is successful
            URL url = new URL("http://localhost:3333/users/login");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setDoOutput(true);
    
            String requestBody = "username=" + username + "&password=" + password;
    
            try (OutputStream os = connection.getOutputStream()) {
                os.write(requestBody.getBytes());
                os.flush();
            }
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner responseScanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();
    
                // Check if login was successful
                if (response.toString().contains("Login successful")) {
                    // Now retrieve the user's full name and role from users.json
                    return fetchUserDetails(username);
                } else {
                    System.out.println(RED + "Login failed. Invalid username or password." + RESET);
                    return false;
                }
            } else {
                System.out.println(RED + "Login failed. Server returned error code: " + responseCode + RESET);
                return false;
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred during login." + RESET);
            return false;
        }
    }
    
    private static boolean fetchUserDetails(String username) {
        try {
            // Load users from the JSON file
            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, String>> users = objectMapper.readValue(
                    new File("src/main/java/com/clothingstore/app/server/data/users.json"),
                    new TypeReference<List<Map<String, String>>>() {}
            );
    
            // Search for the user in the list
            for (Map<String, String> user : users) {
                if (user.get("username").equals(username)) {
                    String fullName = user.get("fullName");
                    String role = user.get("role");
                    userBranchId = user.get("branchId");
                    System.out.println(GREEN + "Welcome " + fullName + ", you are logged in as a " + role + "." + RESET);
                    return true;
                }
            }
    
            // If no match was found
            System.out.println(RED + "User not found in the records." + RESET);
            return false;
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while fetching user details." + RESET);
            return false;
        }
    }
    
    private static void handleEmployeesManagement(Scanner scanner) {
        try {
            URL url = new URL("http://localhost:3333/users?branchId=" + userBranchId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner responseScanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();

                // Parse the JSON response into a List of Customer objects
                ObjectMapper objectMapper = new ObjectMapper();
                List<User> employees = objectMapper.readValue(response.toString(), new TypeReference<List<User>>() {});

                // Display customer details in a table format
                printEmployeesTable(employees);

            } else {
                System.out.println(RED + "Failed to fetch employee details. Server returned error code: " + responseCode + RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while fetching employee details." + RESET);
        }
    }

    private static void printEmployeesTable(List<User> employees) {
        // Print table headers
        System.out.printf(CYAN + "%-15s %-20s %-15s %-20s %-15s %-10s %-25s\n" + RESET, 
                        "Employee ID", "Full Name", "Account Number", "Employee Number", "Role", "BranchId", "PhoneNumber");
        System.out.println(CYAN + "------------------------------------------------------------------------------------------" + RESET);

        // Print each customer in a row
        for (User employee : employees) {
            System.out.printf("%-15s %-20s %-15s %-20s %-15s %-10s %-25s\n", 
            employee.getUserId(), 
            employee.getFullName(), 
            employee.getAccountNumber(), 
            employee.getEmployeeNumber(),
            employee.getRole(),
            employee.getBranchId(),
            employee.getPhoneNumber()); 
        }
    }
   
    private static void handleCustomerManagement(Scanner scanner) {
        try {
            URL url = new URL("http://localhost:3333/customers");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner responseScanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();

                // Parse the JSON response into a List of Customer objects
                ObjectMapper objectMapper = new ObjectMapper();
                List<Customer> customers = objectMapper.readValue(response.toString(), new TypeReference<List<Customer>>() {});

                // Display customer details in a table format
                printCustomerTable(customers);

            } else {
                System.out.println(RED + "Failed to fetch customer details. Server returned error code: " + responseCode + RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while fetching customer details." + RESET);
        }
    }

    private static void printCustomerTable(List<Customer> customers) {
        // Print table headers
        System.out.printf(CYAN + "%-15s %-25s %-15s %-15s %-15s\n" + RESET, 
                        "Customer ID", "Full Name", "Postal Code", "Phone Number", "Customer Type");
        System.out.println(CYAN + "------------------------------------------------------------------------------------------" + RESET);

        // Print each customer in a row
        for (Customer customer : customers) {
            System.out.printf("%-15s %-25s %-15s %-15s %-15s\n", 
                            customer.getCustomerId(), 
                            customer.getFullName(), 
                            customer.getPostalCode(), 
                            customer.getPhoneNumber(), 
                            customer.getCustomerType());
        }
    }

       // Fetch and display products
       private static void handleProductManagement(Scanner scanner) {
        try {
            URL url = new URL("http://localhost:3333/products?branchId=" + userBranchId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            System.out.println("responseCode: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) {
                Scanner responseScanner = new Scanner(connection.getInputStream());
                StringBuilder response = new StringBuilder();
                while (responseScanner.hasNextLine()) {
                    response.append(responseScanner.nextLine());
                }
                responseScanner.close();

                // Parse the JSON response into a List of Product objects
                ObjectMapper objectMapper = new ObjectMapper();
                List<Product> products = objectMapper.readValue(response.toString(), new TypeReference<List<Product>>() {});
                // Display product details in a table format
                printProductTable(products);

                // Now allow user to buy or sell products
                boolean done = false;
                while (!done) {
                    System.out.println(CYAN + "What would you like to do?" + RESET);
                    System.out.println("1. Buy Product");
                    System.out.println("2. Sell Product");
                    System.out.println("3. Go Back");
                    System.out.println(CYAN + "Select an option: " + RESET);
                    int choice = Integer.parseInt(scanner.nextLine());

                    switch (choice) {
                        case 1:
                            buyProduct(scanner);
                            break;
                        case 2:
                            sellProduct(scanner);
                            break;
                        case 3:
                            done = true; // Go back to the main menu
                            break;
                        default:
                            System.out.println(RED + "Invalid choice. Please try again." + RESET);
                    }
                }

            } else {
                System.out.println(RED + "Failed to fetch product details. Server returned error code: " + responseCode + RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while fetching product details." + RESET);
        }
    }

    private static void printProductTable(List<Product> products) {
        // Print table headers
        System.out.printf(CYAN + "%-20s %-20s %-20s %-20s\n" + RESET,
                "Product ID", "Product Name", "Stock Quantity", "Price");
        System.out.println(CYAN + "----------------------------------------------------" + RESET);

        // Print each product in a row
        for (Product product : products) {
            System.out.printf("%-20s %-20s %-20d $%.5f\n",
                    product.getProductId(),
                    product.getProductName(),
                    product.getStockQuantity(),
                    product.getPrice());
        }
    }

    // Handle buying a product
    private static void buyProduct(Scanner scanner) {
    System.out.println(CYAN + "Enter Product ID to buy: " + RESET);
    String productId = scanner.nextLine();

    System.out.println(CYAN + "Enter quantity: " + RESET);
    int quantity = Integer.parseInt(scanner.nextLine());

    try {
        URL url = new URL("http://localhost:3333/products/buy?productId=" + productId + "&quantity=" + quantity);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            @SuppressWarnings("resource")
            String responseMessage = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println(GREEN + responseMessage + RESET);
        } else {
            @SuppressWarnings("resource")
            String errorMessage = new BufferedReader(new InputStreamReader(connection.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
            System.out.println(RED + "Failed to buy product: " + errorMessage + RESET);
        }
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println(RED + "Error occurred while buying the product." + RESET);
    }
}


    // Handle selling a product
    private static void sellProduct(Scanner scanner) {
        System.out.println(CYAN + "Enter Product ID to sell: " + RESET);
        String productId = scanner.nextLine();

        System.out.println(CYAN + "Enter quantity: " + RESET);
        int quantity = Integer.parseInt(scanner.nextLine());

        try {
            URL url = new URL("http://localhost:3333/products/sell?productId=" + productId + "&quantity=" + quantity);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(GREEN + "Sale successful!" + RESET);
            } else {
                System.out.println(RED + "Failed to sell product. Server returned error code: " + responseCode + RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while selling the product." + RESET);
        }
    }

    private static void runMainMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            printMenu();
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    handleProductManagement(scanner);
                    break;
                case 2:
                    handleCustomerManagement(scanner);
                    break;
                case 3:
                    // Implement sales reports
                    break;
                case 4:
                    handleEmployeesManagement(scanner);
                    break;
                case 5:
                    // Implement chat system
                    break;
                case 6:
                    // Implement system logs
                    break;
                case 0:
                    running = false;
                    continue;
                default:
                    System.out.println(RED + "Invalid choice. Please try again." + RESET);
                    continue;
            }

            System.out.println(YELLOW + "Press Enter to display the menu..." + RESET);
            scanner.nextLine();
        }
    }

    private static void printMenu() {
        System.out.println(BLUE + "---------------------------" + RESET);
        System.out.println(BLUE + " Clothing Store Management " + RESET);
        System.out.println(BLUE + "---------------------------" + RESET);
        System.out.println("1. Inventory Management");
        System.out.println("2. Customer Management");
        System.out.println("3. Sales Reports");
        System.out.println("4. Employee Management");
        System.out.println("5. Chat System");
        System.out.println("6. System Logs");
        System.out.println("0. Exit");
        System.out.print("Enter your choice: ");
    }
}