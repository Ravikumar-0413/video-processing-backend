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
public class VideoCompressionService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    // ─────────────────────────────────────────
    //  COMPRESS VIDEO
    //  quality → low / medium / high
    // ─────────────────────────────────────────
    public File compressVideo(MultipartFile file,
                              String quality) throws IOException {

        // STEP 1 — Save uploaded file to temp/input
        File inputFile = saveInputFile(file);

        // STEP 2 — Create output file path
        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/compressed_"
            + UUID.randomUUID()
            + ".mp4"
        );

        // STEP 3 — Get CRF value based on quality
        String crfValue = getCrfValue(quality);

        // STEP 4 — Build FFmpeg compress command
        // ffmpeg -i input.mp4 -vcodec libx264 -crf 28 output.mp4
        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",       inputFile.getAbsolutePath(),
            "-vcodec",  "libx264",
            "-crf",     crfValue,
            "-acodec",  "aac",
            "-strict",  "experimental",
            outputFile.getAbsolutePath()
        );

        // STEP 5 — Execute FFmpeg command
        boolean success = ffmpegExecutor.execute(command);

        // STEP 6 — Check result
        if (!success) {
            throw new RuntimeException("Video compression failed!");
        }

        // STEP 7 — Delete input temp file
        inputFile.delete();

        return outputFile;
    }

    // ─────────────────────────────────────────
    //  GET CRF VALUE
    //  low    → 18 (high quality, less compression)
    //  medium → 28 (balanced)
    //  high   → 35 (small file, lower quality)
    // ─────────────────────────────────────────
    private String getCrfValue(String quality) {
        switch (quality.toLowerCase()) {
            case "low":    return "18";
            case "medium": return "28";
            case "high":   return "35";
            default:       return "28";  // default medium
        }
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