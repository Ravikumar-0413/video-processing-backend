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

    public File trimVideo(MultipartFile file,
                          String startTime,
                          String endTime) throws IOException {

        File inputFile = saveInputFile(file);

        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/trimmed_"
            + UUID.randomUUID()
            + ".mp4"
        );

        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",  inputFile.getAbsolutePath(),
            "-ss", startTime,
            "-to", endTime,
            "-c",  "copy",
            outputFile.getAbsolutePath()
        );

        boolean success = ffmpegExecutor.execute(command);

        if (!success) {
            throw new RuntimeException("Video trimming failed!");
        }

        inputFile.delete();
        return outputFile;
    }

    public File cropVideo(MultipartFile file,
                          int width,
                          int height,
                          int x,
                          int y) throws IOException {

        File inputFile = saveInputFile(file);

        File outputFile = new File(
            ffmpegExecutor.getOutputDir()
            + "/cropped_"
            + UUID.randomUUID()
            + ".mp4"
        );

        String cropFilter = "crop=" + width + ":" + height + ":" + x + ":" + y;

        List<String> command = Arrays.asList(
            ffmpegExecutor.getFfmpegPath(),
            "-i",  inputFile.getAbsolutePath(),
            "-vf", cropFilter,
            outputFile.getAbsolutePath()
        );

        boolean success = ffmpegExecutor.execute(command);

        if (!success) {
            throw new RuntimeException("Video cropping failed!");
        }

        inputFile.delete();
        return outputFile;
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