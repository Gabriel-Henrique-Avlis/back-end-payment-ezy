FROM eclipse-temurin:17-jdk-jammy AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY app/mvnw .
COPY app/.mvn .mvn
COPY app/pom.xml .

# Grant execute permission to mvnw
RUN chmod +x mvnw

# Download dependencies and build
COPY app/src ./src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copy the built JAR from builder stage
COPY --from=builder /app/target/payment-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=3 \
    CMD java -cp app.jar org.springframework.boot.loader.JarLauncher -version || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
