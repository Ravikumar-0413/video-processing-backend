# Use Java 21 with FFmpeg pre-installed
FROM jrottenberg/ffmpeg:4.4-ubuntu AS ffmpeg
FROM eclipse-temurin:21-jdk-jammy

# Copy FFmpeg from ffmpeg image
COPY --from=ffmpeg /usr/local /usr/local

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Install Maven and build the project
RUN apt-get update && \
    apt-get install -y maven && \
    mvn clean package -DskipTests

# Create temp folders
RUN mkdir -p temp/input temp/output

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/video-processing-backend-0.0.1-SNAPSHOT.jar"]