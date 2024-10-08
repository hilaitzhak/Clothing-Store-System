package com.clothingstore.app.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.models.User;
// import com.clothingstore.app.server.chat.ChatServer;

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
            // Send a GET request to the server to fetch user details by username
            URL url = new URL("http://localhost:3333/users/" + username + "/details");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Read the response from the server
                @SuppressWarnings("resource")
                String response = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
    
                // Parse the response into a Map<String, Object>
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> userDetails = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
    
                // Print user details (e.g., full name and role)
                String fullName = (String) userDetails.get("fullName");
                String role = (String) userDetails.get("role");
                userBranchId = (String) userDetails.get("branchId");
                System.out.println(GREEN + "Welcome " + fullName + ", you are logged in as a " + role + "." + RESET);
    
                return true;
            } else {
                System.out.println(RED + "Failed to fetch user details. Server returned error code: " + responseCode + RESET);
                return false;
            }
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
        System.out.println(CYAN + "------------------------------------------------------------------------------------------------------" + RESET);

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
            URL url = new URL("http://localhost:3333/customers/all");
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
        System.out.println(CYAN + "Enter Customer ID: " + RESET);
        String customerId = scanner.nextLine();
    
        // Fetch and display customer details
        Map<String, Object> customerDetails = fetchCustomerDetails(customerId);
        if (customerDetails == null) {
            System.out.println(RED + "Customer not found. Cannot proceed with purchase." + RESET);
            return; // Stop the purchase process if the customer is not found
        }
    
        // Extract and display customer information from the Map
        String fullName = (String) customerDetails.get("fullName");
        double salePercentage = (double) customerDetails.get("salePercentage");
        // int points = (int) customerDetails.get("points");
    
        System.out.println(CYAN + fullName + " has a " + salePercentage + "% discount " + RESET);
    
        System.out.println(CYAN + "Enter Product ID to buy: " + RESET);
        String productId = scanner.nextLine();
    
        System.out.println(CYAN + "Enter quantity: " + RESET);
        int quantity = Integer.parseInt(scanner.nextLine());
    
        try {
            // URL for the buy product request
            URL url = new URL("http://localhost:3333/products/buy?productId=" + productId + "&quantity=" + quantity + "&customerId=" + customerId);
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
        System.out.println(CYAN + "Enter Customer ID: " + RESET);
        String customerId = scanner.nextLine();
    
        // Fetch and display customer details
        Map<String, Object> customerDetails = fetchCustomerDetails(customerId);
        if (customerDetails == null) {
            System.out.println(RED + "Customer not found. Cannot proceed with selling." + RESET);
            return; // Stop the sale process if the customer is not found
        }
    
        // Extract and display customer information
        String fullName = (String) customerDetails.get("fullName");
        double salePercentage = (double) customerDetails.get("salePercentage");
        // int points = (int) customerDetails.get("points");
    
        System.out.println(CYAN + "To " + fullName + " has " + salePercentage + "% sale and " + RESET);
    
        System.out.println(CYAN + "Enter Product ID to sell: " + RESET);
        String productId = scanner.nextLine();
    
        System.out.println(CYAN + "Enter quantity: " + RESET);
        int quantity = Integer.parseInt(scanner.nextLine());
    
        try {
            URL url = new URL("http://localhost:3333/products/sell?productId=" + productId + "&quantity=" + quantity + "&customerId=" + customerId);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                System.out.println(GREEN + "Sell successful!" + RESET);
            } else {
                System.out.println(RED + "Failed to sell product. Server returned error code: " + responseCode + RESET);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while selling the product." + RESET);
        }
    }
    

    private static Map<String, Object> fetchCustomerDetails(String customerId) {
        try {
            URL url = new URL("http://localhost:3333/customers/" + customerId + "/details");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
    
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Parse the response as a Map<String, Object>
                @SuppressWarnings("resource")
                String response = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
    
                ObjectMapper objectMapper = new ObjectMapper();
                // Deserialize JSON response into a Map<String, Object>
                return objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            } else {
                System.out.println(RED + "Failed to fetch customer details. Server returned error code: " + responseCode + RESET);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(RED + "Error occurred while fetching customer details." + RESET);
            return null;
        }
    }
    

    private static final String API_BASE_URL = "http://localhost:3333/chat";
    private static final int CHAT_WIDTH = 70;
    private static final int POLLING_INTERVAL = 1000; // 1 second

    private static void handleChatSystem(Scanner scanner) {
        boolean chatting = true;
        while (chatting) {
            System.out.println(CYAN + "Chat Management" + RESET);
            System.out.println("1. Request Chat");
            System.out.println("2. Join Chat");
            System.out.println("3. Send Message");
            System.out.println("4. Close Chat");
            System.out.println("5. Check Chat Status");
            System.out.println("6. Join Chat as Shift Manager");
            System.out.println("7. View Active Chats");
            System.out.println("0. Back to Main Menu");
            System.out.print("Enter your choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    requestChat(scanner);
                    break;
                case 2:
                    joinChat(scanner);
                    break;
                case 3:
                    sendMessage(scanner);
                    break;
                case 4:
                    closeChat(scanner);
                    break;
                case 5:
                    checkChatStatus(scanner);
                    break;
                case 6:
                    joinChatAsManager(scanner);
                    break;
                case 7:
                    viewActiveChats();
                    break;
                case 0:
                    chatting = false;
                    break;
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }


    private static void requestChat(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            URL url = new URL(API_BASE_URL + "/request?username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error requesting chat: " + e.getMessage() + RESET);
        }
    }

    private static void joinChat(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            URL url = new URL(API_BASE_URL + "/join?username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error joining chat: " + e.getMessage() + RESET);
        }
    }

    // private static void sendMessage(Scanner scanner) {
    //     System.out.print("Enter chat ID: ");
    //     String chatId = scanner.nextLine();
    //     System.out.print("Enter your username: ");
    //     String username = scanner.nextLine();
    //     System.out.print("Enter your message: ");
    //     String message = scanner.nextLine();

    //     try {
    //         URL url = new URL(API_BASE_URL + "/message?chatId=" + URLEncoder.encode(chatId, "UTF-8") +
    //                 "&username=" + URLEncoder.encode(username, "UTF-8") +
    //                 "&message=" + URLEncoder.encode(message, "UTF-8"));
    //         HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    //         conn.setRequestMethod("POST");

    //         BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
    //         String response = reader.readLine();
    //         System.out.println(GREEN + response + RESET);
    //     } catch (Exception e) {
    //         System.out.println(RED + "Error sending message: " + e.getMessage() + RESET);
    //     }
    // }

        private static void sendMessage(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter chat ID: ");
        String chatId = scanner.nextLine();

        List<String> messages = new ArrayList<>();
        boolean chatting = true;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(() -> {
            List<String> newMessages = fetchMessages(chatId, username);
            if (!newMessages.equals(messages)) {
                messages.clear();
                messages.addAll(newMessages);
                displayChatWindow(messages);
            }
        }, 0, POLLING_INTERVAL, TimeUnit.MILLISECONDS);

        while (chatting) {
            // System.out.print("Enter message (or 'EXIT' to leave): ");
            String input = scanner.nextLine();

            if ("EXIT".equalsIgnoreCase(input)) {
                chatting = false;
            } else {
                sendMessageToServer(chatId, username, input);
            }
        }

        executor.shutdownNow();
        System.out.println("Chat ended. Press Enter to return to the main menu.");
        scanner.nextLine();
    }

    private static void displayChatWindow(List<String> messages) {
        clearConsole();
        System.out.println("+" + "-".repeat(CHAT_WIDTH) + "+");
        for (String message : messages) {
            // Remove the square brackets and quotes from the message
            message = message.replaceAll("[\\[\\]\"]", "");
            String[] parts = message.split(":", 2);
            if (parts.length == 2) {
                String sender = parts[0].trim();
                String content = parts[1].trim();

                System.out.printf("| %-10s: %-" + (CHAT_WIDTH - 14) + "s |\n", sender, content);
            }
        }
        System.out.println("+" + "-".repeat(CHAT_WIDTH) + "+");
        System.out.print("Enter message (or 'EXIT' to leave): ");
    }

    private static void sendMessageToServer(String chatId, String username, String message) {
        try {
            URL url = new URL(API_BASE_URL + "/message?chatId=" + URLEncoder.encode(chatId, "UTF-8") +
                    "&username=" + URLEncoder.encode(username, "UTF-8") +
                    "&message=" + URLEncoder.encode(message, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.getInputStream().close();
        } catch (Exception e) {
            System.out.println(RED + "Error sending message: " + e.getMessage() + RESET);
        }
    }

    private static List<String> fetchMessages(String chatId, String username) {
        List<String> messages = new ArrayList<>();
        try {
            URL url = new URL(API_BASE_URL + "/messages?chatId=" + URLEncoder.encode(chatId, "UTF-8") +
                    "&username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                messages.add(line);
            }
        } catch (Exception e) {
            System.out.println(RED + "Error fetching messages: " + e.getMessage() + RESET);
        }
        return messages;
    }

    private static void clearConsole() {
        try {
            final String os = System.getProperty("os.name");
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                Runtime.getRuntime().exec("clear");
            }
        } catch (Exception e) {
            // If we can't clear the console, just print a bunch of newlines
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
        }
    }

    private static void closeChat(Scanner scanner) {
        System.out.print("Enter chat ID: ");
        String chatId = scanner.nextLine();

        try {
            URL url = new URL(API_BASE_URL + "/close?chatId=" + URLEncoder.encode(chatId, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error closing chat: " + e.getMessage() + RESET);
        }
    }

    private static void checkChatStatus(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();

        try {
            URL url = new URL(API_BASE_URL + "/status?username=" + URLEncoder.encode(username, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error checking chat status: " + e.getMessage() + RESET);
        }
    }
    
    private static void joinChatAsManager(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter chat ID to join: ");
        String chatId = scanner.nextLine();

        try {
            URL url = new URL(API_BASE_URL + "/chat/join-as-manager?username=" + URLEncoder.encode(username, "UTF-8") +
                    "&chatId=" + URLEncoder.encode(chatId, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response = reader.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (Exception e) {
            System.out.println(RED + "Error joining chat as manager: " + e.getMessage() + RESET);
        }
    }

    private static void viewActiveChats() {
        try {
            URL url = new URL(API_BASE_URL + "/chat/active");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String response;
            System.out.println(GREEN + "Active Chats:" + RESET);
            while ((response = reader.readLine()) != null) {
                System.out.println(response);
            }
        } catch (Exception e) {
            System.out.println(RED + "Error viewing active chats: " + e.getMessage() + RESET);
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
                    handleChatSystem(scanner);
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
        System.out.println("1. Manage Products");
        System.out.println("2. Manage Customers");
        System.out.println("3. Sales Reports");
        System.out.println("4. Manage Employees");
        System.out.println("5. Chat System");
        System.out.println("6. System Logs");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }
}