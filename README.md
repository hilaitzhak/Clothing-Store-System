# Clothing Store Management System

## Overview

The Clothing Store Management System is a console application developed in Java using Maven and the Spring Framework. This application provides a comprehensive solution for managing various aspects of a clothing store, including inventory management, customer management, product transactions, and an internal chat system.

## Features

- **Manage Stock:** 
  - View current stock levels for all products.

- **Manage Products:**
  - Retrieve product information based on various criteria.

- **Manage Customers:**
  - Categorize customers into new, returning, and VIP types, with specific purchase paths for each.

- **Buy and Sell Products:**
  - Facilitate product purchases and sales transactions.

- **Chat System:**
  - Enable communication among employees through a chat system.
  - Allow shift managers to join existing chats.
  - Implement a queue system to match free employees with chat requests.

## Technologies Used

- **Java** 
- **Maven**
- **Spring Framework**

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Maven 3.6 or higher

### Installation

1. Clone the repository to your local machine:

   ```bash
   git clone https://github.com/hilaitzhak/Clothing-Store-System.git
   ```

2. Navigate to the project directory:

   ```bash
   cd clothing-store-management
   ```

3. Run the server:

   ```bash
   ./start-server.sh
   ```

4. Run the client:

   ```bash
   ./start-client.sh
   ```