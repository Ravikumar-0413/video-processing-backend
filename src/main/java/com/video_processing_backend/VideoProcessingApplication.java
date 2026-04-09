package com.video_processing_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;

@SpringBootApplication
@EnableScheduling
public class VideoProcessingApplication {

    public static void main(String[] args) {

        // ── Create temp folders on startup ──
        createTempFolders();

        SpringApplication.run(VideoProcessingApplication.class, args);

        System.out.println("===========================================");
        System.out.println("  Video Processing Backend Started!       ");
        System.out.println("  URL : http://localhost:8080             ");
        System.out.println("===========================================");
    }

    /**
     * Auto creates temp/input and temp/output
     * folders when application starts
     * temp/input  → temporarily save uploaded video here
                so FFmpeg can READ it

		temp/output → FFmpeg saves processed video here
                so we can SEND it back to user
     */
    private static void createTempFolders() {
        String[] folders = {
            "temp/input",
            "temp/output"
        };

        for (String folder : folders) {
            File dir = new File(folder);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (created) {
                    System.out.println("✅ Created folder: " + folder);
                }
            } else {
                System.out.println("📁 Folder exists: " + folder);
            }
        }
    }
}
//```
//
/*
 * VideoProcessingApplication is the main entry point of the entire application.
 * When the application starts, this file executes first.
 *
 * This file is responsible for 3 things:
 * 1. Creating temp folders (temp/input and temp/output) before any API is called.
 * 2. Starting the Spring Boot server on port 8080.
 * 3. Printing a startup success message in the console.
 *
 * @SpringBootApplication - Marks this as the main Spring Boot class.
 * @EnableScheduling - Enables scheduled tasks like auto cleanup of temp files.
 *
 * temp/input  - Uploaded videos are saved here so FFmpeg can read and process them.
 * temp/output - Processed videos are saved here so we can send them back to the user.
 */
//---
//
//## 📌 Explanation
//
//| Part | Purpose |
//|---|---|
//| `@SpringBootApplication` | Main Spring Boot entry point |
//| `@EnableScheduling` | Activates scheduled tasks for temp file cleanup |
//| `createTempFolders()` | Auto creates `temp/input` and `temp/output` on startup |
//| `SpringApplication.run()` | Boots the entire application |
//
//---
//
//## 🖥️ Console Output When App Starts
//```
//✅ Created folder: temp/input
//✅ Created folder: temp/output
//
//===========================================
//  Video Processing Backend Started!
//  URL : http://localhost:8080
//===========================================