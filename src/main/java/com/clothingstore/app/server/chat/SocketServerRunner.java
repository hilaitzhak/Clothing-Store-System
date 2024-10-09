// package com.clothingstore.app.server.chat;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// @Component
// public class SocketServerRunner implements CommandLineRunner {

//     @Autowired
//     private SocketServer socketServer;

//     @Override
//     public void run(String... args) {
//         new Thread(() -> socketServer.start()).start();
//     }
// }