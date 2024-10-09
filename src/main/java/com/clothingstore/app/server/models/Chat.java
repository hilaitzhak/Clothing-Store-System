package com.clothingstore.app.server.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.Objects;
import java.util.Collections;

public class Chat {
    private final String chatId;
    private final String initiatorUsername;
    private volatile String recipientUsername;
    private final LocalDateTime startTime;
    private volatile LocalDateTime endTime;
    private volatile String status;
    private final CopyOnWriteArrayList<String> messages;
    private volatile String managerUsername;

    public Chat(String chatId, String initiatorUsername) {
        this.chatId = Objects.requireNonNull(chatId, "Chat ID cannot be null");
        this.initiatorUsername = Objects.requireNonNull(initiatorUsername, "Initiator username cannot be null");
        this.startTime = LocalDateTime.now();
        this.status = "WAITING";
        this.messages = new CopyOnWriteArrayList<>();
    }

    public String getChatId() {
        return chatId;
    }

    public String getInitiatorUsername() {
        return initiatorUsername;
    }

    public String getRecipientUsername() {
        return recipientUsername;
    }

    public void setRecipientUsername(String recipientUsername) {
        this.recipientUsername = recipientUsername;
        if (recipientUsername != null) {
            this.status = "ACTIVE";
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
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
        this.status = Objects.requireNonNull(status, "Status cannot be null");
    }

    public List<String> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void addMessage(String message) {
        messages.add(Objects.requireNonNull(message, "Message cannot be null"));
    }

    public String getManagerUsername() {
        return managerUsername;
    }

    public void setManagerUsername(String managerUsername) {
        this.managerUsername = managerUsername;
    }

    public List<String> getParticipants() {
        List<String> participants = new ArrayList<>(3);
        participants.add(initiatorUsername);
        if (recipientUsername != null) {
            participants.add(recipientUsername);
        }
        if (managerUsername != null) {
            participants.add(managerUsername);
        }
        return participants;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Chat))
            return false;
        Chat chat = (Chat) o;
        return chatId.equals(chat.chatId);
    }

    @Override
    public int hashCode() {
        return chatId.hashCode();
    }

    @Override
    public String toString() {
        return "Chat{" +
                "chatId='" + chatId + '\'' +
                ", initiatorUsername='" + initiatorUsername + '\'' +
                ", recipientUsername='" + recipientUsername + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}