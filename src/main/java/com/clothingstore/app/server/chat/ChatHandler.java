// package com.clothingstore.app.server.chat;

// import com.clothingstore.app.server.services.UserService;
// import java.io.BufferedReader;
// import java.io.IOException;
// import java.io.InputStreamReader;
// import java.io.PrintWriter;
// import java.net.Socket;

// public class ChatHandler implements Runnable {
//     private Socket clientSocket;
//     private ChatServer chatServer;
//     private UserService userService;
//     private PrintWriter out;
//     private BufferedReader in;
//     private String username;

//     public ChatHandler(Socket socket, ChatServer server, UserService userService) {
//         this.clientSocket = socket;
//         this.chatServer = server;
//         this.userService = userService;
//     }

//     @Override
//     public void run() {
//         try {
//             out = new PrintWriter(clientSocket.getOutputStream(), true);
//             in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

//             // Authenticate user
//             String credentials = in.readLine();
//             String[] parts = credentials.split(":");
//             if (parts.length != 2 || !userService.login(parts[0], parts[1])) {
//                 out.println("AUTH_FAILED");
//                 return;
//             }

//             username = parts[0];
//             if (chatServer.isUserActive(username)) {
//                 out.println("ALREADY_ACTIVE");
//                 return;
//             }

//             chatServer.addActiveChat(username, this);
//             out.println("CONNECTED");

//             String inputLine;
//             while ((inputLine = in.readLine()) != null) {
//                 if ("EXIT".equals(inputLine)) {
//                     break;
//                 }
//                 // Handle chat messages here
//                 System.out.println(username + ": " + inputLine);
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         } finally {
//             try {
//                 chatServer.removeActiveChat(username);
//                 clientSocket.close();
//             } catch (IOException e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     public void sendMessage(String message) {
//         out.println(message);
//     }
// }