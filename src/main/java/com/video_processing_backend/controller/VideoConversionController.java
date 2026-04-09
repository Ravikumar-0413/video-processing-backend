package com.video_processing_backend.controller;

import com.video_processing_backend.service.VideoConversionService;
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
@RequestMapping("/api/video/convert")
public class VideoConversionController {

    @Autowired
    private VideoConversionService videoConversionService;

    // ─────────────────────────────────────────
    //  CONVERT TO MP4
    //  POST /api/video/convert/mp4
    // ─────────────────────────────────────────
    @PostMapping("/mp4")
    public ResponseEntity<Resource> convertToMp4(
            @RequestParam("file") MultipartFile file) {
        try {

            // STEP 1 — Check if format is supported
            if (!videoConversionService
                    .isSupportedFormat(file.getOriginalFilename())) {

                return ResponseEntity.badRequest().build();
            }

            // STEP 2 — Call service
            File outputFile = videoConversionService
                                .convertToMp4(file);

            // STEP 3 — Return file as download response
            return buildFileResponse(outputFile, "converted_video.mp4");

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
//| Convert to MP4 | `POST` | `/api/video/convert/mp4` | `file` |
//
//---
//
//## 📌 Supported Input Formats
//
//| Format | Extension |
//|---|---|
//| AVI | `.avi` |
//| MKV | `.mkv` |
//| MOV | `.mov` |
//| WMV | `.wmv` |
//| FLV | `.flv` |
//| WEBM | `.webm` |
//| 3GP | `.3gp` |
//
//---
//
//## 🧪 Test In Postman
//```
//Method  → POST
//URL     → http://localhost:8080/api/video/convert/mp4
//Body    → form-data
//  file  → (select your .avi / .mkv / .mov etc file)
//```
//
//---
//
//## 🔗 Complete Flow
//```
//Postman/Frontend
//      │
//      │  POST /api/video/convert/mp4
//      ▼
//VideoConversionController
//      │
//      ├── Check format supported?
//      │   NO  → 400 Bad Request
//      │   YES → continue
//      │
//      │  convertToMp4()
//      ▼
//VideoConversionService
//      │
//      ├── Save file      → temp/input/uuid_video.avi
//      ├── Build command  → ffmpeg -i input -vcodec libx264 -acodec aac output.mp4
//      ├── Execute        → FFmpegExecutor.execute()
//      └── Return file    → temp/output/converted_uuid.mp4
//      │
//      ▼
//ResponseEntity (file download)