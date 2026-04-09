 package com.video_processing_backend.service;

import com.video_processing_backend.util.FFmpegExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class VideoEditingService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    // ─────────────────────────────────────────
    //  TRIM VIDEO
    // ─────────────────────────────────────────
    public File trimVideo(MultipartFile file,
                          String startTime,
                          String endTime) throws IOException {

        // STEP 1 — Save uploaded file to temp/input
        File inputFile = saveInputFile(file);

        // STEP 2 — Create output file path
        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/trimmed_"
            + UUID.randomUUID()
            + ".mp4"
        );

        // STEP 3 — Build FFmpeg trim command
        // ffmpeg -i input.mp4 -ss 00:00:10 -to 00:00:30 -c copy output.mp4
        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",  inputFile.getAbsolutePath(),
            "-ss", startTime,
            "-to", endTime,
            "-c",  "copy",
            outputFile.getAbsolutePath()
        );

        // STEP 4 — Execute FFmpeg command
        boolean success = ffmpegExecutor.execute(command);

        // STEP 5 — Check result
        if (!success) {
            throw new RuntimeException("Video trimming failed!");
        }

        // STEP 6 — Delete input temp file
        inputFile.delete();

        return outputFile;
    }

    // ─────────────────────────────────────────
    //  CROP VIDEO
    // ─────────────────────────────────────────
    public File cropVideo(MultipartFile file,
                          int width,
                          int height,
                          int x,
                          int y) throws IOException {

        // STEP 1 — Save uploaded file to temp/input
        File inputFile = saveInputFile(file);

        // STEP 2 — Create output file path
        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/cropped_"
            + UUID.randomUUID()
            + ".mp4"
        );

        // STEP 3 — Build FFmpeg crop command
        // ffmpeg -i input.mp4 -vf "crop=640:480:0:0" output.mp4
        String cropFilter = "crop=" + width + ":" + height + ":" + x + ":" + y;

        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",  inputFile.getAbsolutePath(),
            "-vf", cropFilter,
            outputFile.getAbsolutePath()
        );

        // STEP 4 — Execute FFmpeg command
        boolean success = ffmpegExecutor.execute(command);

        // STEP 5 — Check result
        if (!success) {
            throw new RuntimeException("Video cropping failed!");
        }

        // STEP 6 — Delete input temp file
        inputFile.delete();

        return outputFile;
    }

    // ─────────────────────────────────────────
    //  HELPER — Save uploaded file to temp/input
    // ─────────────────────────────────────────
    private File saveInputFile(MultipartFile file) throws IOException {
        File inputFile = new File(
            ffmpegExecutor.getInputDir()
            + "/"
            + UUID.randomUUID()
            + "_"
            + file.getOriginalFilename()
        );
        file.transferTo(inputFile);
        return inputFile;
    }
}
//```
//
//---
//
//## 🧪 Test In Postman
//
//### Trim Video:
//```
//Method  → POST
//URL     → http://localhost:8080/api/video/edit/trim
//Body    → form-data
//  file       → (select your video file)
//  startTime  → 00:00:05
//  endTime    → 00:00:20
//```
//
//### Crop Video:
//```
//Method  → POST
//URL     → http://localhost:8080/api/video/edit/crop
//Body    → form-data
//  file    → (select your video file)
//  width   → 640
//  height  → 480
//  x       → 0
//  y       → 0