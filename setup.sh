#!/bin/bash

# Navigate to the backend directory
cd ludonova-backend

# Create main directory structure
mkdir -p src/main/java/com/bht/ludonova
mkdir -p src/main/resources
mkdir -p src/test/java/com/bht/ludonova
mkdir -p src/test/resources

# Create package directories
cd src/main/java/com/bht/ludonova
mkdir config
mkdir controller
mkdir model
mkdir repository
mkdir service
mkdir security
mkdir dto
mkdir exception

# Create application.properties
cd ../../resources
echo "# Server Configuration
server.port=8080

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/ludonova
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
jwt.secret=your-secret-key-here
jwt.expiration=86400000

# CORS Configuration
spring.web.cors.allowed-origins=http://localhost:3000
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*

# Logging Configuration
logging.level.com.bht.ludonova=DEBUG" > application.properties

# Create main application class
cd ../java/com/bht/ludonova
echo "package com.bht.ludonova;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LudoNovaApplication {
    public static void main(String[] args) {
        SpringApplication.run(LudoNovaApplication.class, args);
    }
}" > LudoNovaApplication.java