# ============================================
# BUILD STAGE
# ============================================
FROM maven:3.9.9-eclipse-temurin-17 AS build

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests

# ============================================
# RUN STAGE
# ============================================
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

# Copy jar from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application with production profile
ENTRYPOINT ["java", "-Dspring.profiles.active=production", "-jar", "app.jar"]