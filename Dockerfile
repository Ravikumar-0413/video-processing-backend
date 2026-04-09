FROM eclipse-temurin:21-jdk-jammy

# Install FFmpeg and Maven
RUN apt-get update && \
    apt-get install -y ffmpeg maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Verify FFmpeg path
RUN which ffmpeg && ffmpeg -version

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

RUN mkdir -p temp/input temp/output

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/video-processing-backend-0.0.1-SNAPSHOT.jar"]