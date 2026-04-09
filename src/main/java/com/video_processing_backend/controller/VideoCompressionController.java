package com.video_processing_backend.controller;

import com.video_processing_backend.service.VideoCompressionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/video/compress")
public class VideoCompressionController {

    @Autowired
    private VideoCompressionService videoCompressionService;

    // ─────────────────────────────────────────
    //  COMPRESS VIDEO
    //  POST /api/video/compress
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<Resource> compressVideo(
            @RequestParam("file")    MultipartFile file,
            @RequestParam(value = "quality",
                          defaultValue = "medium") String quality) {
        try {

            // Call service
            File outputFile = videoCompressionService
                                .compressVideo(file, quality);

            // Return file as download response
            return buildFileResponse(outputFile, "compressed_video.mp4");

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────
    //  HELPER — Build file download response
    // ─────────────────────────────────────────
    private ResponseEntity<Resource> buildFileResponse(File file,
                                                        String filename) {
        Resource resource = new FileSystemResource(file);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("video/mp4"))
                .body(resource);
    }
}
//```
//
//---
//
//## 📌 API Endpoint
//
//| Operation | Method | Endpoint | Params |
//|---|---|---|---|
//| Compress Video | `POST` | `/api/video/compress` | `file`, `quality` |
//
//---
//
//## 📌 Quality Levels
//
//| Quality | CRF Value | Result |
//|---|---|---|
//| `low` | `18` | High quality, less compression |
//| `medium` | `28` | Balanced — default |
//| `high` | `35` | Small file, lower quality |
//
//---
//
//## 🧪 Test In Postman
//```
//Method  → POST
//URL     → http://localhost:8080/api/video/compress
//Body    → form-data
//  file     → (select your video file)
//  quality  → medium
//```
//
//---
//
//## 🔗 Complete Flow
//```
//Postman/Frontend
//      │
//      │  POST /api/video/compress
//      ▼
//VideoCompressionController
//      │
//      │  compressVideo()
//      ▼
//VideoCompressionService
//      │
//      ├── Save file      → temp/input/uuid_video.mp4
//      ├── Get CRF value  → medium = 28
//      ├── Build command  → ffmpeg -i input -vcodec libx264 -crf 28 output
//      ├── Execute        → FFmpegExecutor.execute()
//      └── Return file    → temp/output/compressed_uuid.mp4
//      │
//      ▼
//ResponseEntity (file download)