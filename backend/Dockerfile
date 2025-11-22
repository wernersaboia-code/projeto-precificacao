# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml first (for better cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copy JAR
COPY --from=build /app/target/*.jar app.jar

# Expose port
EXPOSE 8080

# Run application with PROD profile
ENTRYPOINT ["sh", "-c", "java -Dspring.profiles.active=prod -Dserver.port=${PORT:-8080} -jar app.jar"]
