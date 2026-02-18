# Multi-stage build to keep runtime image slim
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
RUN useradd -ms /bin/bash appuser
COPY --from=builder /app/target/*.jar app.jar
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS=""
EXPOSE 8080
USER appuser
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar app.jar"]

