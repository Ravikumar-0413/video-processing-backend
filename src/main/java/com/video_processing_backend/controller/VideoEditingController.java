 package com.video_processing_backend.controller;

import com.video_processing_backend.service.VideoEditingService;
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
@RequestMapping("/api/video/edit")
public class VideoEditingController {

    @Autowired
    private VideoEditingService videoEditingService;

    // ─────────────────────────────────────────
    //  TRIM VIDEO
    //  POST /api/video/edit/trim
    // ─────────────────────────────────────────
    @PostMapping("/trim")
    public ResponseEntity<Resource> trimVideo(
            @RequestParam("file")      MultipartFile file,
            @RequestParam("startTime") String startTime,
            @RequestParam("endTime")   String endTime) {
        try {

            File outputFile = videoEditingService.trimVideo(
                file, startTime, endTime
            );

            return buildFileResponse(outputFile, "trimmed_video.mp4");

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ─────────────────────────────────────────
    //  CROP VIDEO
    //  POST /api/video/edit/crop
    // ─────────────────────────────────────────
    @PostMapping("/crop")
    public ResponseEntity<Resource> cropVideo(
            @RequestParam("file")   MultipartFile file,
            @RequestParam("width")  int width,
            @RequestParam("height") int height,
            @RequestParam("x")      int x,
            @RequestParam("y")      int y) {
        try {

            File outputFile = videoEditingService.cropVideo(
                file, width, height, x, y
            );

            return buildFileResponse(outputFile, "cropped_video.mp4");

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