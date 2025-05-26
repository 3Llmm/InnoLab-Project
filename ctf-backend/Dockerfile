

FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests=true -Dcheckstyle.skip=true


# Stage 2: Run the JAR
FROM openjdk:21-jdk
WORKDIR /app
COPY --from=builder /app/target/ctfbackend-*.jar app.jar
COPY src/main/resources/files /app/files

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

