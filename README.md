💰 Financial Transaction Audit System










A secure, scalable backend system built using Spring Boot to monitor financial transactions, detect fraud, and maintain a complete audit trail.

🚀 Features
🔐 JWT Authentication & Authorization
Secure login & registration
Role-based access control (ADMIN, AUDITOR, USER)
💳 Transaction Management
Create, view, and manage transactions
Pagination & sorting support
🚨 Fraud Detection Engine

Rule-based fraud detection:

Large transactions
Rapid transactions
Unusual hours
Duplicate transactions

Auto Status Handling:

🔴 BLOCKED
🟠 FLAGGED
🟢 COMPLETED
🔵 APPROVED
📊 Dashboard API
Aggregated transaction insights
🔔 Alert System
Fraud alerts with resolution workflow
📜 Audit Logging
Tracks all system activities
Ensures traceability
⚠️ Exception Handling
Custom exceptions with proper HTTP status codes
🏗️ Architecture
Client (Postman / Frontend)
        ↓
Controller Layer (REST APIs)
        ↓
Service Layer (Business Logic)
        ↓
Fraud Detection Engine
        ↓
Repository Layer (JPA)
        ↓
MySQL Database
🛠️ Tech Stack
Layer	Technology
Backend	Spring Boot
Security	Spring Security + JWT
Database	MySQL
ORM	Spring Data JPA
Build Tool	Maven
Testing	Postman
🔐 Authentication
Uses JWT (JSON Web Token)
Stateless session management
Token validity: 24 hours
📡 API Endpoints
🔑 Auth APIs
POST /api/auth/register   → Register user
POST /api/auth/login      → Login & get JWT
💳 Transaction APIs
POST   /api/transactions              → Create transaction
GET    /api/transactions              → Get all (paginated)
GET    /api/transactions/{id}         → Get by ID
GET    /api/transactions/flagged      → Flagged transactions
PUT    /api/transactions/{id}/approve → Approve transaction
GET    /api/transactions/dashboard    → Dashboard data
🚨 Alert APIs
GET    /api/alerts              → Get all alerts
PUT    /api/alerts/{id}/resolve → Resolve alert
📜 Audit APIs
GET /api/audit → Get audit logs
📊 Sample Request
Create Transaction
POST /api/transactions
Authorization: Bearer <JWT>
{
  "amount": 15000,
  "recipientAccount": "1234567890",
  "timestamp": "2026-05-22T10:30:00"
}
📌 Fraud Detection Rules
Rule	Condition
Large Transaction	Amount ≥ 10,000
Rapid Transactions	>5 transactions in 10 minutes
Unusual Hours	12 AM – 5 AM
Duplicate	Same amount + recipient
