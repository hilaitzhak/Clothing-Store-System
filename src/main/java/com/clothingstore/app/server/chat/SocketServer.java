package com.clothingstore.app.server.chat;

import com.clothingstore.app.server.services.ChatService;
import com.clothingstore.app.server.services.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import com.clothingstore.app.server.models.Chat;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import org.slf4j.Logger;

@Component
public class SocketServer {

    private static final Logger logger = LoggerFactory.getLogger(SocketServer.class);

    @Value("${socket.server.port}")
    private int port;

    @Autowired
    private ChatService chatService;

    private ServerSocket serverSocket;
    private final ExecutorService pool = Executors.newCachedThreadPool();
    private volatile boolean running = false;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                running = true;
                logger.info("Socket server started on port {}", port);

                while (running) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        pool.execute(new ClientHandler(clientSocket, chatService));
                        logger.info("New client connected: {}", clientSocket.getInetAddress().getHostAddress());
                    } catch (IOException e) {
                        if (running) {
                            logger.error("Error accepting client connection: {}", e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                logger.error("Could not start server on port {}: {}", port, e.getMessage());
            }
        }).start();
    }


    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            pool.shutdown();
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
            logger.info("Socket server stopped");
        } catch (IOException | InterruptedException e) {
            logger.error("Error stopping server: {}", e.getMessage());
            pool.shutdownNow();
        }
    }

    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private ChatService chatService;
        private BufferedReader in;
        private PrintWriter out;

        public ClientHandler(Socket socket, ChatService chatService) {
            this.clientSocket = socket;
            this.chatService = chatService;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream(), true);

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String[] parts = inputLine.split(":");
                    String command = parts[0];

                    switch (command) {
                        case "REQUEST_CHAT":
                            if (parts.length < 2) {
                                out.println("Invalid REQUEST_CHAT command format. Usage: REQUEST_CHAT:username");
                            } else {
                                handleRequestChat(parts[1]);
                            }
                            break;
                        case "JOIN_CHAT":
                            if (parts.length < 2) {
                                out.println("Invalid JOIN_CHAT command format. Usage: JOIN_CHAT:username");
                            } else {
                                handleJoinChat(parts[1]);
                            }
                            break;
                        case "ENTER_CHAT":
                            if (parts.length < 3) {
                                out.println("Invalid ENTER_CHAT command format. Usage: ENTER_CHAT:chatId:message");
                            } else {
                                handleEnterChat(parts[1], parts[2]);
                            }
                            break;
                        case "SEND_MESSAGE":
                            if (parts.length < 5) {
                                out.println(
                                        "Invalid SEND_MESSAGE command format. Usage: SEND_MESSAGE:chatId:username:message:timestamp");
                            } else {
                                handleSendMessage(parts[1], parts[2], parts[3], parts[4]);
                            }
                            break;
                        case "LEAVE_CHAT":
                            if (parts.length < 3) {
                                out.println("Invalid LEAVE_CHAT command format. Usage: LEAVE_CHAT:chatId:username");
                            } else {
                                handleLeaveChat(parts[1], parts[2]);
                            }
                            break;
                        case "JOIN_AS_MANAGER":
                            if (parts.length < 3) {
                                out.println(
                                        "Invalid JOIN_AS_MANAGER command format. Usage: JOIN_AS_MANAGER:chatId:managerUsername");
                            } else {
                                handleJoinAsManager(parts[1], parts[2]);
                            }
                            break;
                        case "VIEW_ACTIVE_CHATS":
                            handleViewActiveChats();
                            break;
                        default:
                            out.println("Unknown command: " + command);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (in != null)
                        in.close();
                    if (out != null)
                        out.close();
                    if (clientSocket != null)
                        clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        
        private void handleSendMessage(String chatId, String username, String message, String timestamp) {
            try {
                chatService.addMessage(chatId, username, message, timestamp);
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
            }
        }
        
        private void handleRequestChat(String username) {
            try {
                Chat chat = chatService.requestChat(username);
                out.println("Chat request added to queue. Chat ID: " + chat.getChatId());
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleJoinChat(String username) {
            try {
                Chat chat = chatService.joinChat(username);
                if (chat == null) {
                    out.println("No waiting chats available.");
                } else {
                    out.println("Joined chat with " + chat.getInitiatorUsername() + ". Chat ID: " + chat.getChatId());
                }
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleEnterChat(String chatId, String username) {
            try {
                chatService.addClientWriter(username, out);
                out.println("CHAT_ENTERED");
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleLeaveChat(String chatId, String username) {
            out.println("Left chat: " + chatId);
        }

        private void handleJoinAsManager(String username, String chatId) {
            try {
                // Handle joining as manager
                boolean success = chatService.joinChatAsManager(chatId, username);
                if (success) {
                    out.println("Manager joined chat ID: " + chatId);
                } else {
                    out.println("Error: Unable to join chat ID " + chatId);
                }
            } catch (IllegalArgumentException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void handleViewActiveChats() {
            try {
                // Fetch active chats
                var activeChats = chatService.getActiveChats();
                if (activeChats.isEmpty()) {
                    out.println("No active chats available.");
                } else {
                    for (Chat chat : activeChats) {
                        out.println("Chat ID: " + chat.getChatId() + ", Participants: " + chat.getParticipants());
                    }
                }
                out.println("END_OF_ACTIVE_CHATS");
            } catch (Exception e) {
                out.println("Error retrieving active chats: " + e.getMessage());
            }
        }
    }
}