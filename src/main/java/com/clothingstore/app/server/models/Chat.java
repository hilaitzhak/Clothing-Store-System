package com.clothingstore.app.server.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Chat {
    private String chatId;
    private String initiatorUsername;
    private String recipientUsername;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status; // "WAITING", "ACTIVE", "CLOSED"
    private List<String> messages;

    public Chat(String chatId, String initiatorUsername) {
        this.chatId = chatId;
        this.initiatorUsername = initiatorUsername;
        this.startTime = LocalDateTime.now();
        this.status = "WAITING";
        this.messages = new ArrayList<>();
    }

    // Getters and setters

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getInitiatorUsername() {
        return initiatorUsername;
    }

    public void setInitiatorUsername(String initiatorUsername) {
        this.initiatorUsername = initiatorUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }
}