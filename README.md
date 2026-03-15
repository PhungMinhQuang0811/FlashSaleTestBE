🚀 Flash Sale System - Technical Test
A high-concurrency e-commerce backend built with Java 21 and Spring Boot 3.x, focusing on race condition handling, secure transactions, and scalable architecture.

🛠 Tech Stack
Backend: Java 21, Spring Boot 3.x, Spring Security (JWT: Access, Refresh, CSRF).

Database: MySQL (Primary), MongoDB (Audit/Logs), Redis (Cart, Rate Limiting, Stock Lock).

Cloud & Utils: Cloudinary (Images), Mailtrap (Mock Email), VNPAY (Payment Gateway).

Architecture: Layered Architecture with Interface-driven development for easy optimization (e.g., swapping DB locks for Redis Lua Scripts).
⚙️ Environment Variables
Create an .env file or set these variables in your IDE:
# Database & Cache
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/flash_sale
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=
REDIS_HOST=localhost
REDIS_PORT=6379
SPRING_DATA_MONGODB_URI=mongodb://localhost:27017/flash_sale

# Security (JWT)
JWT_ACCESS_SECRET_KEY=your_access_secret
JWT_REFRESH_SECRET_KEY=your_refresh_secret
JWT_CSRF_SECRET_KEY=your_csrf_secret

# Integrations
CLOUDINARY_CLOUD_NAME=your_name
CLOUDINARY_API_KEY=your_key
CLOUDINARY_API_SECRET=your_secret
MAILTRAP_USERNAME=your_user
MAILTRAP_PASSWORD=your_pass

# Payment (VNPAY)
VNPAY_SECRET_KEY=your_vnpay_secret
TMN_CODE=your_tmn_code
VNPAY_RETURN_URL=http://localhost:3000/payment-callback

🚀 How to Run
1. Prerequisites
JDK 21 & Maven 3.x

Docker & Docker Compose installed.

2. Infrastructure Setup (Docker)
This command will spin up MySQL, Redis, and MongoDB automatically:
docker-compose -f environment/docker-compose-dev.yml up -d

4. Execution
# 1. Clone the repo
git clone https://github.com/PhungMinhQuang0811/FlashSaleTestBE.git

# 2. Build the project
mvn clean install

# 3. Run the application
mvn spring-boot:run

4. API Documentation
Access Swagger UI at: http://localhost:8080/swagger-ui/index.html
