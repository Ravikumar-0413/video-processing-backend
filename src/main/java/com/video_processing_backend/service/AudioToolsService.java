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

    public File extractMp3(MultipartFile file,
                           String bitrate) throws IOException {

        File inputFile = saveInputFile(file);

        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/audio_"
            + UUID.randomUUID()
            + ".mp3"
        );

        String validBitrate = getValidBitrate(bitrate);

        System.out.println("FFmpeg Path: " + ffmpegExecutor.getFfmpegPath());
        System.out.println("Input File: " + inputFile.getAbsolutePath());
        System.out.println("Output File: " + outputFile.getAbsolutePath());
        System.out.println("Input Exists: " + inputFile.exists());

        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",       inputFile.getAbsolutePath(),
            "-vn",
            "-acodec",  "mp3",
            "-ab",      validBitrate,
            "-y",
            outputFile.getAbsolutePath()
        );

        boolean success = ffmpegExecutor.execute(command);

        if (!success) {
            throw new RuntimeException("Audio extraction failed!");
        }

        inputFile.delete();
        return outputFile;
    }

    private String getValidBitrate(String bitrate) {
        List<String> validBitrates = Arrays.asList(
            "128k", "192k", "256k", "320k"
        );
        if (bitrate != null &&
            validBitrates.contains(bitrate.toLowerCase())) {
            return bitrate;
        }
        return "192k";
    }

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