# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy pom.xml and download dependencies (cached layer)
COPY msin_bourse_enligne/pom.xml .
COPY msin_bourse_enligne/.mvn .mvn
COPY msin_bourse_enligne/mvnw .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY msin_bourse_enligne/src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Render provides PORT environment variable
# Use it to configure the server port
EXPOSE ${PORT:-8080}

# Run the application with PORT environment variable
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8080} -jar /app/app.jar"]