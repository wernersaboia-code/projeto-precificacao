# Dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace/app

# Copiar arquivos do Maven
COPY pom.xml .
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Imagem final
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar JAR gerado
COPY --from=build /workspace/app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]