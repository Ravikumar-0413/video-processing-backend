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
public class AudioToolsService {

    @Autowired
    private FFmpegExecutor ffmpegExecutor;

    // ─────────────────────────────────────────
    //  EXTRACT MP3 FROM VIDEO
    //  bitrate → 128k / 192k / 256k / 320k
    // ─────────────────────────────────────────
    public File extractMp3(MultipartFile file,
                           String bitrate) throws IOException {

        // STEP 1 — Save uploaded file to temp/input
        File inputFile = saveInputFile(file);

        // STEP 2 — Create output file path
        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/audio_"
            + UUID.randomUUID()
            + ".mp3"
        );

        // STEP 3 — Validate bitrate
        String validBitrate = getValidBitrate(bitrate);

        // STEP 4 — Build FFmpeg extract audio command
        // ffmpeg -i input.mp4 -vn -acodec mp3 -ab 192k output.mp3
        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",       inputFile.getAbsolutePath(),
            "-vn",                                    // no video
            "-acodec",  "mp3",                        // audio codec mp3
            "-ab",      validBitrate,                 // audio bitrate
            "-y",                                     // overwrite if exists
            outputFile.getAbsolutePath()
        );

        // STEP 5 — Execute FFmpeg command
        boolean success = ffmpegExecutor.execute(command);

        // STEP 6 — Check result
        if (!success) {
            throw new RuntimeException("Audio extraction failed!");
        }

        // STEP 7 — Delete input temp file
        inputFile.delete();

        return outputFile;
    }

    // ─────────────────────────────────────────
    //  GET VALID BITRATE
    //  128k → standard quality
    //  192k → good quality      ← default
    //  256k → high quality
    //  320k → best quality
    // ─────────────────────────────────────────
    private String getValidBitrate(String bitrate) {
        List<String> validBitrates = Arrays.asList(
            "128k", "192k", "256k", "320k"
        );
        if (bitrate != null &&
            validBitrates.contains(bitrate.toLowerCase())) {
            return bitrate;
        }
        return "192k"; // default
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