// package com.clothingstore.app.server.chat;

// import com.clothingstore.app.server.services.UserService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Component;

// import java.io.IOException;
// import java.net.ServerSocket;
// import java.net.Socket;
// import java.util.concurrent.ConcurrentHashMap;
// import java.util.concurrent.ConcurrentLinkedQueue;
// import java.util.concurrent.ExecutorService;
// import java.util.concurrent.Executors;

// @Component
// public class ChatServer {
//     private static final int PORT = 8888;
//     private ServerSocket serverSocket;
//     private ExecutorService pool;
//     private ConcurrentHashMap<String, ChatHandler> activeChats;
//     private ConcurrentLinkedQueue<String> waitingUsers;

//     @Autowired
//     private UserService userService;

//     public ChatServer() {
//         pool = Executors.newCachedThreadPool();
//         activeChats = new ConcurrentHashMap<>();
//         waitingUsers = new ConcurrentLinkedQueue<>();
//     }

//     public void start() {
//         try {
//             serverSocket = new ServerSocket(PORT);
//             System.out.println("Chat Server started on port " + PORT);

//             while (true) {
//                 Socket clientSocket = serverSocket.accept();
//                 pool.execute(new ChatHandler(clientSocket, this, userService));
//             }
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//     }

//         public void addToWaitingList(String username) {
//         waitingUsers.offer(username);
//     }

//     public String getNextWaitingUser() {
//         return waitingUsers.poll();
//     }

//     public void addActiveChat(String username, ChatHandler handler) {
//         activeChats.put(username, handler);
//     }

//     public void removeActiveChat(String username) {
//         activeChats.remove(username);
//     }

//     public boolean isUserActive(String username) {
//         return activeChats.containsKey(username);
//     }

//     public ChatHandler getActiveChatHandler(String username) {
//         return activeChats.get(username);
//     }
// }