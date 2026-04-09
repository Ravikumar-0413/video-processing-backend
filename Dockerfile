FROM eclipse-temurin:21-jdk-jammy

# Install FFmpeg and Maven directly
RUN apt-get update && \
    apt-get install -y ffmpeg maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify FFmpeg installed correctly
RUN ffmpeg -version

# Set working directory
WORKDIR /app

# Copy Maven files
COPY pom.xml .
COPY src ./src

# Build the project
RUN mvn clean package -DskipTests

# Create temp folders
RUN mkdir -p temp/input temp/output

# Expose port 8080
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "target/video-processing-backend-0.0.1-SNAPSHOT.jar"]