# WARP.md

This file provides guidance to WARP (warp.dev) when working with code in this repository.

## Project Overview

Easy-Jmeter is a performance automation testing platform built on top of JMeter. It provides a web-based interface for managing test cases, distributed load testing, real-time data monitoring, test result analysis, and historical data querying. The platform uses a microservices architecture with Vue.js frontend and Spring Boot backend.

## Architecture

### Core Components
- **API Backend**: Spring Boot application (`/api`) handling business logic, JMeter integration, and SocketIO communication
- **Web Frontend**: Vue.js application (`/web`) providing the user interface  
- **Database Layer**: MySQL for business data, MongoDB for detailed test results, InfluxDB for real-time metrics
- **File Storage**: MinIO for test scripts, data files, and reports
- **Communication**: SocketIO for real-time communication between server and agents

### Key Architecture Patterns
- **Server-Agent Pattern**: Central server manages multiple agent machines for distributed testing
- **Multi-Database Strategy**: Different databases optimized for specific data types (relational, document, time-series)
- **Real-time Communication**: SocketIO enables live test monitoring and control
- **File-based Test Management**: JMX files stored in MinIO for version control and distribution

### Agent Communication
The platform uses SocketIO for bi-directional communication between server and agents:
- Server sends test execution commands to agents
- Agents report test progress and metrics back to server
- Configuration controlled by `socket.server.enable` (server mode) and `socket.client.enable` (agent mode)

## Development Commands

### Backend (API)
```bash
# Build the project
cd api && mvn clean package

# Run in development mode (server)
cd api && mvn spring-boot:run -Dspring.profiles.active=dev

# Run tests
cd api && mvn test

# Generate code (MyBatis Plus)
cd api && mvn test -Dtest=CodeGenerator
```

### Frontend (Web)
```bash
# Install dependencies
cd web && npm install

# Development server
cd web && npm run serve

# Build for production
cd web && npm run build

# Run tests
cd web && npm run test:unit

# Lint code
cd web && npm run lint
```

### Docker Deployment
```bash
# Build and start all services
docker-compose up -d

# Build backend JAR first
cd api && mvn clean package

# Build frontend dist first  
cd web && npm run build
```

## Configuration Management

### Environment Profiles
- **Development**: `application-dev.yml` - Local development settings
- **Production**: `application-prod.yml` - Production deployment settings
- **Main Config**: `application.yml` - Common settings and profile activation

### Key Configuration Areas
- **Database Connections**: MySQL, MongoDB, InfluxDB connection settings
- **MinIO Storage**: File storage endpoint and credentials  
- **SocketIO Communication**: Server/client mode and connection URLs
- **Security**: JWT token settings and authentication
- **Scheduled Tasks**: Cron expressions for cleanup and monitoring jobs

### Frontend Environment Variables
- **Development**: `.env.development` - Local API endpoints
- **Production**: `.env.production` - Production API endpoints
- **SocketIO URLs**: Real-time communication endpoints

## Database Schema

### MySQL (Business Data)
Stores user management, project structure, test case definitions, and task metadata using MyBatis Plus ORM.

### MongoDB (Test Results)
Stores detailed JMeter test results and reports using Spring Data MongoDB.

### InfluxDB (Time-Series Metrics)
Stores real-time performance metrics during test execution for monitoring and analysis.

## Testing Strategy

### JMeter Integration
- Test cases stored as JMX files in MinIO
- Dynamic parameter injection for test data
- Real-time metrics collection during execution
- Distributed test execution across multiple agents

### Running Specific Tests
```bash
# Run a specific test class
cd api && mvn test -Dtest=TaskControllerTest

# Run tests for a specific package
cd api && mvn test -Dtest="com.zhao.easyJmeter.service.**"

# Run frontend unit tests for specific component
cd web && npm test -- --testNamePattern="TaskComponent"
```

## File Structure Key Points

### Backend Structure
- `controller/` - REST API endpoints divided into v1 (public) and cms (admin)
- `service/` - Business logic layer with clear separation of concerns
- `dto/` - Data transfer objects for API requests/responses organized by feature
- `model/` - Database entity classes
- `repository/` - Data access layer for MongoDB operations
- `mapper/` - MyBatis mappers for MySQL operations
- `common/` - Shared utilities including JMeter integration classes

### Frontend Structure  
- `view/` - Page components organized by feature (case, task, machine, project, etc.)
- `store/` - Vuex state management with socket integration
- `router/` - Vue Router configuration
- `lin/` - Lin-CMS integration components

## Development Workflow

### Server vs Agent Mode
The same Spring Boot application can run as either server or agent:
- **Server Mode**: Set `socket.server.enable=true` in configuration
- **Agent Mode**: Set `socket.client.enable=true` and configure `serverUrl`

### Real-time Features
The platform provides real-time test monitoring through:
- SocketIO connections for live updates
- InfluxDB time-series data for performance metrics  
- Vue.js frontend with reactive data binding for live dashboards

### File Management
All test-related files (scripts, data, results) are managed through MinIO:
- Automatic file upload/download for test distribution
- Version control through file naming conventions
- Public bucket access for result file sharing
