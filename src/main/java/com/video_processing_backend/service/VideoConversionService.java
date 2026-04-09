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
public class VideoConversionService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    // ─────────────────────────────────────────
    //  CONVERT TO MP4
    //  Supports → .avi .mkv .mov .wmv
    //             .flv .webm .3gp
    // ─────────────────────────────────────────
    public File convertToMp4(MultipartFile file) throws IOException {

        // STEP 1 — Save uploaded file to temp/input
        File inputFile = saveInputFile(file);

        // STEP 2 — Create output file path
        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/converted_"
            + UUID.randomUUID()
            + ".mp4"
        );

        // STEP 3 — Build FFmpeg conversion command
        // ffmpeg -i input.avi -vcodec libx264 -acodec aac output.mp4
        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",       inputFile.getAbsolutePath(),
            "-vcodec",  "libx264",
            "-acodec",  "aac",
            "-strict",  "experimental",
            "-y",                                   // overwrite if exists
            outputFile.getAbsolutePath()
        );

        // STEP 4 — Execute FFmpeg command
        boolean success = ffmpegExecutor.execute(command);

        // STEP 5 — Check result
        if (!success) {
            throw new RuntimeException("Video conversion failed!");
        }

        // STEP 6 — Delete input temp file
        inputFile.delete();

        return outputFile;
    }

    // ─────────────────────────────────────────
    //  GET FILE EXTENSION
    //  Extracts extension from filename
    // ─────────────────────────────────────────
    public String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "unknown";
        }
        return filename.substring(
            filename.lastIndexOf(".") + 1
        ).toLowerCase();
    }

    // ─────────────────────────────────────────
    //  IS SUPPORTED FORMAT
    //  Check if input format is supported
    // ─────────────────────────────────────────
    public boolean isSupportedFormat(String filename) {
        String ext = getFileExtension(filename);
        List<String> supported = Arrays.asList(
            "avi", "mkv", "mov",
            "wmv", "flv", "webm", "3gp"
        );
        return supported.contains(ext);
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