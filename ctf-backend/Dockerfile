FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src /app/src
RUN mvn clean package -DskipTests=true -Dcheckstyle.skip=true


##Stage 2
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/ctfbackend-*.jar app.jar
COPY src/main/resources/files /app/files

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
