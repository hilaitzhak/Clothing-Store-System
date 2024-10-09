package com.clothingstore.app.client;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.io.PrintWriter;
import java.net.Socket;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.clothingstore.app.server.models.Customer;
import com.clothingstore.app.server.models.Product;
import com.clothingstore.app.server.models.User;

public class Client {
    private static final String RESET = "\033[0m";
    private static final String GREEN = "\033[0;32m";
    private static final String RED = "\033[0;31m";
    private static final String BLUE = "\033[0;34m";
    private static final String YELLOW = "\033[0;33m";
    private static final String CYAN = "\033[0;36m";

    private static final int MAX_LOGIN_ATTEMPTS = 3;
    private static String userBranchId;
    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private static String currentUser;
    private static List<String> chatMessages = Collections.synchronizedList(new ArrayList<>());


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

                ObjectMapper objectMapper = new ObjectMapper();
                List<User> employees = objectMapper.readValue(response.toString(), new TypeReference<List<User>>() {});

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
        System.out.printf(CYAN + "%-15s %-20s %-15s %-20s %-15s %-10s %-25s\n" + RESET, 
                        "Employee ID", "Full Name", "Account Number", "Employee Number", "Role", "BranchId", "PhoneNumber");
        System.out.println(CYAN + "------------------------------------------------------------------------------------------------------" + RESET);

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

                ObjectMapper objectMapper = new ObjectMapper();
                List<Customer> customers = objectMapper.readValue(response.toString(), new TypeReference<List<Customer>>() {});

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
        System.out.printf(CYAN + "%-15s %-25s %-15s %-15s %-15s\n" + RESET, 
                        "Customer ID", "Full Name", "Postal Code", "Phone Number", "Customer Type");
        System.out.println(CYAN + "------------------------------------------------------------------------------------------" + RESET);

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

                ObjectMapper objectMapper = new ObjectMapper();
                List<Product> products = objectMapper.readValue(response.toString(), new TypeReference<List<Product>>() {});
                printProductTable(products);

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
                            done = true;
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
        System.out.printf(CYAN + "%-20s %-20s %-20s %-20s\n" + RESET,
                "Product ID", "Product Name", "Stock Quantity", "Price");
        System.out.println(CYAN + "----------------------------------------------------" + RESET);

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
            return;
        }
    
        // Extract and display customer information from the Map
        String fullName = (String) customerDetails.get("fullName");
        double salePercentage = (double) customerDetails.get("salePercentage");
    
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
            return;
        }
    
        // Extract and display customer information
        String fullName = (String) customerDetails.get("fullName");
        double salePercentage = (double) customerDetails.get("salePercentage");
    
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
                @SuppressWarnings("resource")
                String response = new BufferedReader(new InputStreamReader(connection.getInputStream()))
                        .lines().collect(Collectors.joining("\n"));
    
                ObjectMapper objectMapper = new ObjectMapper();
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
    

    private static final String CHAT_SERVER_ADDRESS = "localhost";
    private static final int CHAT_SERVER_PORT = 3334;
    private static final int CHAT_WIDTH = 80;

    private static void handleChatSystem(Scanner scanner) {
        try {
            connectToChatServer();

            boolean chatting = true;
            while (chatting) {
                displayChatMenu();
                int choice = getUserChoice(scanner);

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
                        joinChatAsManager(scanner);
                        break;
                    case 6:
                        viewActiveChats();
                        break;
                    case 0:
                        chatting = false;
                        break;
                    default:
                        System.out.println(RED + "Invalid choice." + RESET);
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "Error in chat system: " + e.getMessage() + RESET);
        } finally {
            closeConnection();
        }
    }

    private static void displayChatMenu() {
        System.out.println(CYAN + "Chat Management" + RESET);
        System.out.println("1. Request Chat");
        System.out.println("2. Join Chat");
        System.out.println("3. Send Message");
        System.out.println("4. Join Chat as Shift Manager");
        System.out.println("5. View Active Chats");
        System.out.println("0. Back to Main Menu");
        System.out.print("Enter your choice: ");
    }

    private static int getUserChoice(Scanner scanner) {
        while (!scanner.hasNextInt()) {
            System.out.println(RED + "Invalid input. Please enter a number." + RESET);
            scanner.next();
        }
        int choice = scanner.nextInt();
        scanner.nextLine();
        return choice;
    }

    private static void connectToChatServer() throws IOException {
        System.out.println("Attempting to connect to chat server at " + CHAT_SERVER_ADDRESS + ":" + CHAT_SERVER_PORT);
        socket = new Socket();
        socket.connect(new InetSocketAddress(CHAT_SERVER_ADDRESS, CHAT_SERVER_PORT), 5000);
        System.out.println("Successfully connected to chat server");
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private static void requestChat(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        out.println("REQUEST_CHAT:" + username);
        try {
            String response = in.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error requesting chat: " + e.getMessage() + RESET);
        }
    }

    private static void joinChat(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        out.println("JOIN_CHAT:" + username);
        try {
            String response = in.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error joining chat: " + e.getMessage() + RESET);
        }
    }

    private static void sendMessage(Scanner scanner) {
        System.out.print("Enter your username: ");
        currentUser = scanner.nextLine();
        System.out.print("Enter chat ID: ");
        String chatId = scanner.nextLine();

        out.println("ENTER_CHAT:" + chatId + ":" + currentUser);
        try {
            String response = in.readLine();
            if (response.equals("CHAT_ENTERED")) {
                System.out.println(GREEN + "Entered chat. Type 'EXIT' to leave." + RESET);
                Thread receiverThread = new Thread(new MessageReceiver());
                receiverThread.start();

                String message;
                while (true) {
                    displayChatWindow();
                    System.out.print("Enter a message or press 'EXIT' to close the chat: ");
                    message = scanner.nextLine();
                    if (message.equalsIgnoreCase("EXIT")) {
                        out.println("LEAVE_CHAT:" + chatId + ":" + currentUser);
                        System.out.println(GREEN + "You have left the chat." + RESET);
                        receiverThread.interrupt(); // Interrupt the receiver thread
                        break;
                    }

                    // Ensure the socket is still connected before sending
                    if (socket.isConnected() && !socket.isClosed()) {
                        String timestamp = getCurrentTimeInTimezone();
                        out.println("SEND_MESSAGE:" + chatId + ":" + currentUser + ":" + message + ":" + timestamp);
                    } else {
                        System.out.println(RED + "Connection closed. Cannot send message." + RESET);
                    }
                }

                // Wait for the receiver thread to finish after leaving the chat
                try {
                    receiverThread.join();
                } catch (InterruptedException e) {
                    System.out.println(RED + "Error waiting for message receiver to finish: " + e.getMessage() + RESET);
                }
            }
        } catch (IOException e) {
            System.out.println(RED + "Error in chat communication: " + e.getMessage() + RESET);
        }
    }

    private static String getCurrentTimeInTimezone() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private static class MessageReceiver implements Runnable {
        @Override
        public void run() {
            String incomingMessage;
            try {
                while (!Thread.currentThread().isInterrupted() && (incomingMessage = in.readLine()) != null) {
                    synchronized (chatMessages) {
                        chatMessages.add(incomingMessage);
                    }
                    System.out.println("Received: " + incomingMessage);
                }
            } catch (IOException e) {
                System.out.println(RED + "Error receiving messages: " + e.getMessage() + RESET);
            }
        }
    }

    private static void displayChatWindow() {
        clearConsole();
        System.out.println(YELLOW + "+" + "-".repeat(CHAT_WIDTH) + "+" + RESET);
        System.out.println(YELLOW + "|" + " ".repeat(CHAT_WIDTH) + "|" + RESET);
        synchronized (chatMessages) {

            for (String message : chatMessages) {
                String[] parts = message.split(":", 3);
                if (parts.length == 3) {
                    String sender = parts[0].trim();
                    String content = parts[1].trim();
                    String timestamp = parts[2].trim();

                    boolean isSender = sender.equals(currentUser);
                    String color = isSender ? BLUE : GREEN;
                    String alignedMessage = String.format("%s%s [%s]:%s %s",
                            color,
                            sender,
                            timestamp,
                            RESET,
                            content);

                    List<String> wrappedLines = wordWrap(alignedMessage, CHAT_WIDTH - 4);

                    for (int i = 0; i < wrappedLines.size(); i++) {
                        String line = wrappedLines.get(i);
                        if (isSender) {
                            System.out.printf(YELLOW + "│%s%-" + (CHAT_WIDTH - 2) + "s│" + RESET + "%n",
                                    (i == 0 ? "" : "  "), line);
                        } else {
                            System.out.printf(YELLOW + "│%-" + (CHAT_WIDTH - 2) + "s%s│" + RESET + "%n",
                                    line, (i == 0 ? "" : "  "));
                        }
                    }
                    System.out.println(YELLOW + "│" + " ".repeat(CHAT_WIDTH - 2) + "│" + RESET);
                }
            }
        }


        System.out.println(YELLOW + "+" + "-".repeat(CHAT_WIDTH) + "+" + RESET);
    }

    private static List<String> wordWrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            if (currentLine.length() + word.length() + 1 > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                if (currentLine.length() > 0) {
                    currentLine.append(" ");
                }
                currentLine.append(word);
            }
        }

        if (currentLine.length() > 0) {
            lines.add(currentLine.toString());
        }

        return lines;
    }

    private static void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void closeChat(Scanner scanner) {
        System.out.print("Enter chat ID: ");
        String chatId = scanner.nextLine();
        out.println("CLOSE_CHAT:" + chatId);
        try {
            String response = in.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error closing chat: " + e.getMessage() + RESET);
        }
    }


    private static void joinChatAsManager(Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter chat ID to join: ");
        String chatId = scanner.nextLine();
        out.println("JOIN_AS_MANAGER:" + username + ":" + chatId);
        try {
            String response = in.readLine();
            System.out.println(GREEN + response + RESET);
        } catch (IOException e) {
            System.out.println(RED + "Error joining chat as manager: " + e.getMessage() + RESET);
        }
    }

    private static void viewActiveChats() {
        out.println("VIEW_ACTIVE_CHATS");
        try {
            System.out.println(GREEN + "Active Chats:" + RESET);
            String response;
            while (!(response = in.readLine()).equals("END_OF_ACTIVE_CHATS")) {
                System.out.println(response);
            }
        } catch (IOException e) {
            System.out.println(RED + "Error viewing active chats: " + e.getMessage() + RESET);
        }
    }

    private static void closeConnection() {
        try {
            if (socket != null && !socket.isClosed()) {
                System.out.println("Closing connection...");
                socket.close();
            }
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        } catch (IOException e) {
            System.out.println(RED + "Error closing connection: " + e.getMessage() + RESET);
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
                    handleEmployeesManagement(scanner);
                    break;
                case 4:
                    handleChatSystem(scanner);
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
        System.out.println("3. Manage Employees");
        System.out.println("4. Chat System");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }
}