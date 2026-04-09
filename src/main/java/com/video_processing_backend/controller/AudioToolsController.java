package com.video_processing_backend.controller;

import com.video_processing_backend.service.AudioToolsService;
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
@RequestMapping("/api/audio")
public class AudioToolsController {

    @Autowired
    private AudioToolsService audioToolsService;

    // ─────────────────────────────────────────
    //  VIDEO TO MP3
    //  POST /api/audio/extract-mp3
    // ─────────────────────────────────────────
    @PostMapping("/extract-mp3")
    public ResponseEntity<Resource> extractMp3(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "bitrate",
                          defaultValue = "192k") String bitrate) {
        try {

            // Call service
            File outputFile = audioToolsService
                                .extractMp3(file, bitrate);

            // Return file as download response
            return buildFileResponse(outputFile, "audio.mp3");

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
                .contentType(MediaType.parseMediaType("audio/mpeg"))
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
//| Video to MP3 | `POST` | `/api/audio/extract-mp3` | `file`, `bitrate` |
//
//---
//
//## 📌 Bitrate Options
//
//| Bitrate | Quality |
//|---|---|
//| `128k` | Standard quality — smaller file |
//| `192k` | Good quality — default ✅ |
//| `256k` | High quality |
//| `320k` | Best quality — larger file |
//
//---
//
//## 🧪 Test In Postman
//```
//Method  → POST
//URL     → http://localhost:8080/api/audio/extract-mp3
//Body    → form-data
//  file     → (select your video file)
//  bitrate  → 192k
//```
//
//---
//
//## 🔗 Complete Flow
//```
//Postman/Frontend
//      │
//      │  POST /api/audio/extract-mp3
//      ▼
//AudioToolsController
//      │
//      │  extractMp3()
//      ▼
//AudioToolsService
//      │
//      ├── Save file      → temp/input/uuid_video.mp4
//      ├── Validate bitrate → 192k
//      ├── Build command  → ffmpeg -i input -vn -acodec mp3 -ab 192k output.mp3
//      ├── Execute        → FFmpegExecutor.execute()
//      └── Return file    → temp/output/audio_uuid.mp3
//      │
//      ▼
//ResponseEntity (audio/mpeg download)
//```
//
//---
//
 