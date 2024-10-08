package com.clothingstore.app.server.controllers;

import com.clothingstore.app.server.models.Chat;
import com.clothingstore.app.server.services.ChatService;
import com.clothingstore.app.server.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private UserService userService;

    @PostMapping("/request")
    public ResponseEntity<?> requestChat(@RequestParam String username) {
        try {
            Chat chat = chatService.requestChat(username);
            return ResponseEntity.ok("Chat request added to queue. Chat ID: " + chat.getChatId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join")
    public ResponseEntity<?> joinChat(@RequestParam String username) {
        try {
            Chat chat = chatService.joinChat(username);
            if (chat == null) {
                return ResponseEntity.ok("No waiting chats available.");
            }
            return ResponseEntity
                    .ok("Joined chat with " + chat.getInitiatorUsername() + ". Chat ID: " + chat.getChatId());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestParam String chatId, @RequestParam String username,
            @RequestParam String message) {
        try {
            chatService.addMessage(chatId, username, message);
            return ResponseEntity.ok("Message sent");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/messages")
    public ResponseEntity<?> getChatMessages(@RequestParam String chatId, @RequestParam String username) {
        logger.info("Received request for messages. ChatId: {}, Username: {}", chatId, username);
        try {
            List<String> messages = chatService.getChatMessages(chatId, username);
            logger.info("Returning {} messages for ChatId: {}", messages.size(), chatId);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Error fetching messages for ChatId: " + chatId, e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/close")
    public ResponseEntity<?> closeChat(@RequestParam String chatId) {
        chatService.closeChat(chatId);
        return ResponseEntity.ok("Chat closed");
    }

    @GetMapping("/status")
    public ResponseEntity<?> getChatStatus(@RequestParam String username) {
        Chat activeChat = chatService.getActiveChat(username);
        if (activeChat != null) {
            return ResponseEntity.ok("In active chat with " +
                    (activeChat.getInitiatorUsername().equals(username) ? activeChat.getRecipientUsername()
                            : activeChat.getInitiatorUsername()));
        } else if (chatService.isUserInActiveChat(username)) {
            return ResponseEntity.ok("Waiting for chat partner");
        } else {
            return ResponseEntity.ok("Not in any chat");
        }
    }
}