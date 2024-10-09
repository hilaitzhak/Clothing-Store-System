package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.Chat;
import com.clothingstore.app.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private UserService userService;

    private ConcurrentHashMap<String, Chat> activeChats = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Chat> waitingChats = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    public Chat requestChat(String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        String chatId = UUID.randomUUID().toString();
        Chat chat = new Chat(chatId, username);
        waitingChats.offer(chat);
        return chat;
    }

    public Chat joinChat(String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }

        Chat chat = waitingChats.poll();
        if (chat == null) {
            return null; // No waiting chats
        }

        chat.setRecipientUsername(username);
        chat.setStatus("ACTIVE");
        activeChats.put(chat.getChatId(), chat);
        return chat;
    }

    // Join an active chat as a shift manager
    public boolean joinChatAsManager(String chatId, String managerUsername) {
        Optional<User> manager = userService.getUserByUsername(managerUsername);
        if (manager.isEmpty() || !manager.get().isShiftManager()) {
            throw new IllegalArgumentException("User is not a shift manager or not found");
        }

        Chat chat = activeChats.get(chatId);
        if (chat == null) {
            return false;
        }

        chat.setManagerUsername(managerUsername);
        broadcastMessage(chat, "Manager " + managerUsername + " has joined the chat.");
        return true;
    }

    // Retrieve active chats
    public List<Chat> getActiveChats() {
        return activeChats.values().stream().collect(Collectors.toList());
    }

    // Retrieve active chat for a specific user
    public Chat getActiveChat(String username) {
        return activeChats.values().stream()
                .filter(chat -> chat.getInitiatorUsername().equals(username) ||
                        chat.getRecipientUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void addMessage(String chatId, String username, String message, String timestamp) {
        Chat chat = activeChats.get(chatId);
        if (chat == null) {
            throw new IllegalArgumentException("Chat not found");
        }
        String fullMessage = username + ":" + message + ":" + timestamp;
        chat.addMessage(fullMessage);

        broadcastMessage(chat, fullMessage);
    }

    private void broadcastMessage(Chat chat, String message) {
        for (String participant : chat.getParticipants()) {
            PrintWriter writer = clientWriters.get(participant);
            if (writer != null) {
                writer.println(message);
                writer.flush();
            }
        }
    }

    // Close a chat and notify participants
    public void closeChat(String chatId) {
        Chat chat = activeChats.remove(chatId);
        if (chat != null) {
            chat.setStatus("CLOSED");
            chat.setEndTime(LocalDateTime.now());

            // Notify participants that the chat has been closed
            broadcastMessage(chat, "CHAT_CLOSED");
        }
    }

    public boolean isUserInActiveChat(String username) {
        return activeChats.values().stream()
                .anyMatch(chat -> chat.getInitiatorUsername().equals(username) ||
                        chat.getRecipientUsername().equals(username));
    }

    // Get chat messages for a user (ensures that only participants or managers can
    // view)
    public List<String> getChatMessages(String chatId, String username) {
        Chat chat = activeChats.get(chatId);
        if (chat == null) {
            throw new IllegalArgumentException("Chat not found");
        }

        if (!chat.getInitiatorUsername().equals(username) &&
                !chat.getRecipientUsername().equals(username) &&
                !userService.isShiftManager(username)) {
            throw new IllegalArgumentException("User is not a participant in this chat");
        }

        return chat.getMessages();
    }

    public void addClientWriter(String username, PrintWriter writer) {
        if (username != null && writer != null) {
            clientWriters.put(username, writer);
        } else {
            throw new IllegalArgumentException("Username or writer cannot be null");
        }
    }

    public void removeClientWriter(String username) {
        if (username != null) {
            clientWriters.remove(username);
        } else {
            throw new IllegalArgumentException("Username cannot be null");
        }
    }
}
