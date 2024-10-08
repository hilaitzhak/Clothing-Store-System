package com.clothingstore.app.server.services;

import com.clothingstore.app.server.models.Chat;
import com.clothingstore.app.server.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class ChatService {

    @Autowired
    private UserService userService;

    private ConcurrentHashMap<String, Chat> activeChats = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<Chat> waitingChats = new ConcurrentLinkedQueue<>();

    public Chat requestChat(String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

        String chatId = UUID.randomUUID().toString();
        Chat chat = new Chat(chatId, username);
        waitingChats.offer(chat);
        return chat;
    }

    public Chat joinChat(String username) {
        Optional<User> user = userService.getUserByUsername(username);
        if (user == null) {
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

    public Chat getActiveChat(String username) {
        return activeChats.values().stream()
                .filter(chat -> chat.getInitiatorUsername().equals(username) ||
                        chat.getRecipientUsername().equals(username))
                .findFirst()
                .orElse(null);
    }

    public void addMessage(String chatId, String username, String message) {
        Chat chat = activeChats.get(chatId);
        if (chat == null) {
            throw new IllegalArgumentException("Chat not found");
        }
        chat.addMessage(username + ": " + message);
    }

    public void closeChat(String chatId) {
        Chat chat = activeChats.remove(chatId);
        if (chat != null) {
            chat.setStatus("CLOSED");
            chat.setEndTime(LocalDateTime.now());
        }
    }

    public boolean isUserInActiveChat(String username) {
        return activeChats.values().stream()
                .anyMatch(chat -> chat.getInitiatorUsername().equals(username) ||
                        chat.getRecipientUsername().equals(username));
    }

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
}