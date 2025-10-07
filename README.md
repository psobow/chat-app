# 🗨️ Chat App - Microservices

A **lightweight real-time messaging system** with **secure authentication**, **contact management**, **instant messaging**, and **read receipts**. 
Built as an **MVP** with a **microservices approach**, it demonstrates **scalable communication** powered by **Spring
Boot 3**, **Keycloak**, **Kafka**, **Redis**, and **PostgreSQL**.

### Key highlights:

- Clear separation of concerns across services
- Asynchronous, event-driven communication with **Kafka**
- Real-time updates via **WebSockets + Redis pub/sub**
- Secure user management with **Keycloak**
- Extensible design for future production scaling

This project is built as **pre-production**: optimized for local/MVP deployment, but designed to extend into a
production-ready system.

## 📋 Requirements (User Stories)

### 🔑 Log In & Out

**As a chat app user**  
I want to log in and out securely,  
**So that** my messages and personal details remain private.

- A user can log in securely
- A user can log out securely
- A user’s credentials are stored securely (via Keycloak)

### 👫 Add a Contact

**As a chat app user**  
I want to add a contact,  
**So that** I can send messages to them.

- Add a contact using their unique identifier (Keycloak ID)
- Assign a custom display name for each contact

### ✉️ Send & Receive Messages

**As a chat app user**  
I want to send and receive messages,  
**So that** I can chat with my contacts.

- Send messages to added contacts
- Receive messages from added contacts

### 👀 Message Read Notifications

**As a chat app user**  
I want to be notified when my messages are read,  
**So that** I have more context during chats.

- Visual indication when a contact reads the message

## 🛠 Tech Stack

- **Java 21**, **Spring Boot 3**
- **Spring WebFlux** — reactive, efficient resource consumption
- **Spring Data JPA + PostgreSQL** — persistence per service
- **Apache Kafka** — asynchronous inter-service communication
- **Redis** — pub/sub for WebSocket message routing
- **Docker & Docker Compose** — containerization & orchestration
- **Keycloak** — authentication and authorization (OIDC/JWT)
- **React + Vite** — frontend client
- **Spring Cloud Gateway** — unified API gateway

## 🏗 Architecture Design

### API Gateway

- Single entry point for frontend → backend communication
- Routes requests to appropriate services
- Handles **JWT validation** with Keycloak
- no database

### Contact Service

- Manages user contacts
- REST Endpoints:
	- `GET /api/v1/contacts` — list all user contacts
	- `POST /api/v1/contacts` — create a new contact
- PostgreSQL for persisting contacts

### Realtime Communication Service (RTCS)

- Exposes **WebSocket API** for messaging
- Handles live events (send, receive, read)
- Uses **Redis pub/sub** to route WebSocket events across instances
- Publishes Kafka topics:
	- `message.sent`
	- `message.read`
- Consumes Kafka topics:
	- `message.sent.persisted`
	- `message.read.persisted`

### Message Service

- Stores and manages chat messages
- REST Endpoints:
	- `GET /api/v1/messages/history/{contactUserId}` — retrieve chat history

- PostgreSQL for persisting messages 
- Publishes Kafka topics:
	- `message.sent.persisted`
	- `message.read.persisted`
- Consumes Kafka topics:
	- `message.sent`
	- `message.read`

### Redis

- Pub-sub system for routing WebSocket messages
- Ensures messages reach the correct RTCS instance
- Enables horizontal scaling of RTCS

### Kafka

- Backbone of asynchronous communication between services
- Separates **event handling** from **persistence**

## 🏛 High-Level Overview

```mermaid
graph LR

%% Top row
UI["Frontend (React + Vite)"] -->|HTTP requests / WebSocket CONNECT| GW["API Gateway"]
UI -->|User Authorization| KC["Keycloak"]

%% Services row
subgraph Contact Service
  CTAPI["REST API<br/>Contacts"]
  CTDB["PostgreSQL"]
end

subgraph Message Service
  MSGAPI["REST API<br/>Message history"]
  MSGDB["PostgreSQL"]
end

subgraph RTS["Realtime Service (RTS)"]
  RTWS["WebSocket handler"]
end

%% Gateway -> Services
GW -->|REST: list/create contacts| CTAPI
GW -->|REST: fetch message history| MSGAPI
GW -->|Establish WebSocket session| RTWS

%% Persistence
CTAPI -->|persist / retrieve contacts| CTDB
MSGAPI -->|persist / retrieve messages| MSGDB

%% Infra
REDIS["Redis<br/>Pub/Sub"]

%% Kafka split to avoid label overlap
subgraph KAFKA[Kafka Topics]
  direction TB
  KAFKA_PUB((Pub))
  KAFKA_SUB((Sub))
end

%% Realtime / events
RTWS -->|broadcast messages between RTS instances| REDIS
RTWS -->|publish message.sent / message.read| KAFKA_PUB
MSGAPI -->|publish message.*.persisted| KAFKA_PUB
KAFKA_SUB -->|consume message.*.persisted| RTWS
KAFKA_SUB -->|consume message.sent / message.read| MSGAPI

```

## REST API

- `GET /api/v1/contacts` → list user’s contacts
- `POST /api/v1/contacts` → create a new contact
- `GET /api/v1/messages/history/{contactUserId}` → fetch message history

All REST endpoints require a valid **JWT token** from Keycloak in the `Authorization` header.

## WebSocket API

### 📡 Connection Acknowledged

Sent by backend when WebSocket connection is established:

```json
{
  "id": "617cb901-9b11-4ef2-8320-37e7a61264de",
  "type": "CONNECTION_ACK",
  "payload": null
}
```

### Runtime Flow - Connection Acknowledged

```mermaid
sequenceDiagram
  autonumber
  participant UI as Frontend (React)
  participant GW as API Gateway
  participant RT as Realtime Service (WS)
  participant KC as Keycloak

  Note over UI: UI holds JWT from Keycloak login

  UI->>GW: WebSocket CONNECT (Authorization: Bearer JWT)
  GW->>KC: Validate JWT (introspect/keys)
  KC-->>GW: OK (token valid)
  GW->>RT: Upgrade & forward connection (user context)
  RT-->>UI: CONNECTION_ACK

```

### ✉️ Message Sent

Represents a new outgoing message:

```json
{
  "id": "617cb901-9b11-4ef2-8320-37e7a61264de",
  "type": "MESSAGE_SENT",
  "payload": {
    "messageId": "064ca7fa-1a71-468a-9a03-b84dfe302228",
    "senderId": "6320cc0b-9139-4849-8576-69de8c4d1b28",
    "receiverId": "445a823f-49f7-41d7-a4b8-444f54495738",
    "text": "Hello World!"
  }
}
```

### Runtime Flow - Message Sent

```mermaid
sequenceDiagram
  autonumber
  participant UI as Frontend (React)
  participant GW as API Gateway
  participant RT as Realtime Service (WS)
  participant K as Kafka
  participant MS as Message Service
  participant DB as PostgreSQL

  Note over UI,GW: User already authenticated via Keycloak

  UI->>GW: WebSocket CONNECT
  GW->>RT: Upgrade + forward
  RT-->>UI: CONNECTION_ACK

  UI->>RT: MESSAGE_SENT {senderId, receiverId, text}
  RT->>K: publish message.sent

  K->>MS: consume message.sent
  MS->>DB: persist(message)
  MS->>K: publish message.sent.persisted

  K->>RT: consume message.sent.persisted
  Note over RT: Deliver to receiver's active socket<br/>via Redis pub/sub if on another RT instance
  RT-->>UI: MESSAGE_SENT (confirm to sender)
  RT-->>UI: MESSAGE_RECEIVED (to receiver)
```

### 👀 Message Read

Represents a read receipt for a message:

```json
{
  "id": "617cb901-9b11-4ef2-8320-37e7a61264de",
  "type": "MESSAGE_READ",
  "payload": {
    "messageId": "064ca7fa-1a71-468a-9a03-b84dfe302228",
    "senderId": "6320cc0b-9139-4849-8576-69de8c4d1b28",
    "receiverId": "445a823f-49f7-41d7-a4b8-444f54495738"
  }
}
```

### Runtime Flow - Message Read

```mermaid
sequenceDiagram
  autonumber
  participant UI as Frontend (React)
  participant GW as API Gateway
  participant RT as Realtime Service (WS)
  participant K as Kafka
  participant MS as Message Service
  participant DB as PostgreSQL

  Note over UI,GW: Receiver already connected WebSocket

  UI->>GW: WebSocket (existing)
  GW->>RT: forward

  UI->>RT: MESSAGE_READ {messageId, senderId, receiverId}
  RT->>K: publish message.read

  K->>MS: consume message.read
  MS->>DB: mark message as READ
  MS->>K: publish message.read.persisted

  K->>RT: consume message.read.persisted
  RT-->>UI: MESSAGE_READ (notify original sender)
  RT-->>UI: MESSAGE_READ_ACK (confirm to receiver)

```

## 🗺️ Future Improvements

- Add **CRUD operations for contacts** (edit/delete)
- Implement **Dead Letter Queue (DLQ)** for Kafka error handling
- Add **monitoring & observability** (Prometheus + Grafana + OpenTelemetry)
- Implement **horizontal scaling** in cloud environments
- Expand message features: attachments, group chats, typing indicators

