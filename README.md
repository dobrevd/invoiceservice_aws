# Invoices Service

## Overview

The **Invoices Service** is a cloud-based microservice designed to handle the import, processing, and management of invoice files for an e-commerce backend system. This service integrates with a legacy system to enable efficient storage and retrieval of invoices, leveraging AWS technologies such as DynamoDB, S3, SQS, and ECS Fargate for scalability, reliability, and performance.

---

### **[Ecommerce](https://github.com/dobrevd/EcommerceECS_CDK_aws)**

This project is an e-commerce platform built using a microservices architecture. The system consists of three main microservices: [ProductsService](https://github.com/dobrevd/productservice_aws), [AuditService](https://github.com/dobrevd/auditservice_aws), and [InvoicesService](https://github.com/dobrevd/invoiceservice_aws). This project demonstrates an infrastructure-as-code (IaC) solution for deploying a scalable e-commerce application using the AWS Cloud Development Kit (CDK) with Java. Each microservice is designed to handle specific aspects of the platform, ensuring modularity, scalability, and maintainability.

### **[ProductsService](https://github.com/dobrevd/productservice_aws)**

This is a cloud-based microservice designed to manage the products offered in an e-commerce platform. This service allows administrators to create, update, and delete products, while customers can search for and retrieve product details. Built with scalability, performance, and reliability in mind, the service leverages AWS technologies such as DynamoDB, SNS, SQS, ECS Fargate, and API Gateway to ensure a seamless user experience.

### **[AuditService](https://github.com/dobrevd/auditservice_aws)**

This is a cloud-based microservice designed to manage the products offered in an e-commerce platform. This service allows administrators to create, update, and delete products, while customers can search for and retrieve product details. Built with scalability, performance, and reliability in mind, the service leverages AWS technologies such as DynamoDB, SNS, SQS, ECS Fargate, and API Gateway to ensure a seamless user experience.

---

## Features

1. **Invoice File Import**:
    - Accepts invoice files from a legacy system using pre-signed S3 URLs.
    - Handles the secure upload of files to an S3 bucket.

2. **Event Notification and Processing**:
    - S3 bucket generates an event notification upon file upload.
    - SQS queue is used to consume these notifications.

3. **Data Persistence**:
    - Processes the uploaded files and persists their data into a DynamoDB table (`invoices`).
    - Ensures efficient querying and retrieval of invoice information.

4. **Transaction Management**:
    - Tracks and logs the import process to ensure traceability.

5. **File Cleanup**:
    - Deletes the imported invoice file from the S3 bucket after successful processing.

6. **Scalability and Monitoring**:
    - Deployed using AWS Fargate for seamless scaling.
    - Logs and metrics collected with AWS X-Ray and CloudWatch for monitoring and debugging.

---

## Architecture

The service follows a robust and modular architecture:

1. **Legacy System Integration**:
    - Pre-signed S3 URLs are generated for secure file uploads.

2. **AWS S3**:
    - Central storage for uploaded invoice files.

3. **AWS SQS**:
    - Event-driven architecture ensures real-time processing of file upload notifications.

4. **AWS DynamoDB**:
    - NoSQL database optimized for fast and efficient storage and retrieval of invoice data.

5. **AWS ECS Fargate**:
    - Serverless container orchestration for running the service.

6. **Load Balancers**:
    - Application and network load balancers distribute traffic and ensure high availability.

---

## Technologies Used

- **Java** (Core Application Logic)
- **Spring Boot** (Backend Framework)
- **AWS CDK** (Infrastructure as Code)
- **AWS Services**:
    - S3 (Storage)
    - DynamoDB (Database)
    - SQS (Event Queue)
    - ECS Fargate (Containerized Service)
    - CloudWatch and X-Ray (Monitoring)