 package com.video_processing_backend.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.video_processing_backend.congif.AppConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.concurrent.TimeUnit;
/*
 * FFmpegExecutor is the core utility class of the entire application.
 * It acts as a bridge between the Spring Boot application and FFmpeg
 * which is a command line tool installed on the computer.
 * Every video processing operation like trim, crop, compress, convert
 * and audio extraction goes through this class.
 * Instead of writing the same ProcessBuilder code in every service class
 * we write it once here and all service classes simply call execute() method.
 * This class gets the FFmpeg path and temp folder paths from AppConfig
 * which reads them from application.properties file.
 */
@Component
public class FFmpegExecutor {
	/*
     * AppConfig is injected here using @Autowired so we can get
     * FFmpeg path and temp folder paths from application.properties.
     * We do not hardcode any paths here so the app works on any computer.
     */
    @Autowired
    private AppConfig appConfig;
    /*
     * TIMEOUT_MINUTES defines how long we wait for FFmpeg to finish processing.
     * If FFmpeg takes more than 10 minutes we stop waiting and return failure.
     * This prevents the application from hanging forever on large video files.
     */
    private static final long TIMEOUT_MINUTES = 10;

    /*
     * getFfmpegPath returns the path of ffmpeg.exe from AppConfig.
     * This is used by all service classes when building FFmpeg commands.
     * Example return value: C:/ffmpeg/bin/ffmpeg.exe
     */
    public String getFfmpegPath() {
        return appConfig.getFfmpegPath();
    }
    /*
     * getFfprobePath returns the path of ffprobe.exe from AppConfig.
     * FFprobe is used to read video information like duration and resolution.
     * Example return value: C:/ffmpeg/bin/ffprobe.exe
     */
    public String getFfprobePath() {
        return appConfig.getFfprobePath();
    }
    /*
     * getInputDir returns the absolute path of temp/input folder.
     * Uploaded video files are saved here so FFmpeg can read and process them.
     * It converts the relative path to absolute path so FFmpeg can find it correctly.
     * If the folder does not exist it creates it automatically before returning.
     */
    public String getInputDir() {
        // ✅ Convert to absolute path
        File dir = new File(appConfig.getInputDir());
        if (!dir.exists()) dir.mkdirs();
        return dir.getAbsolutePath();
    }
    /*
     * getOutputDir returns the absolute path of temp/output folder.
     * FFmpeg saves all processed video and audio files here.
     * We then read the file from here and send it back to the user.
     * It converts the relative path to absolute path so FFmpeg can find it correctly.
     * If the folder does not exist it creates it automatically before returning.
     */
    public String getOutputDir() {
        // ✅ Convert to absolute path
        File dir = new File(appConfig.getOutputDir());
        if (!dir.exists()) dir.mkdirs();
        return dir.getAbsolutePath();
    }

    /*
     * execute() is the main method that runs any FFmpeg command.
     * It is called by every service class for all video processing operations.
     * It receives a list of command arguments like ffmpeg, -i, input.mp4 and so on.
     * It uses ProcessBuilder to run FFmpeg as an OS level process on the computer.
     * redirectErrorStream merges FFmpeg error logs into the main output stream.
     * readOutput() is called in a separate thread to continuously read FFmpeg logs
     * and prevent the output buffer from filling up which would cause a deadlock freeze.
     * waitFor() makes Java wait until FFmpeg finishes processing the video.
     * exitValue() returns 0 if FFmpeg succeeded and any other number if it failed.
     * Returns true if processing was successful and false if it failed.
     */
    public boolean execute(List<String> command) {
        try {
            System.out.println("▶ Running FFmpeg command: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            readOutput(process.getInputStream());

            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            int exitCode = process.exitValue();

            System.out.println("▶ FFmpeg exit code: " + exitCode);

            return finished && exitCode == 0;

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFmpeg execution failed: " + e.getMessage(), e);
        }
    }
    /*
     * executeAndCapture() works the same way as execute() but instead of just
     * running the command it also captures and returns the output as a String.
     * This is used when we need to read information from FFprobe like
     * video duration, resolution or codec details.
     * The output is collected line by line and returned as a single String.
     */
    public String executeAndCapture(List<String> command) {
        StringBuilder output = new StringBuilder();
        try {
            System.out.println("▶ Running FFprobe command: " + command);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            try (BufferedReader reader =
                     new BufferedReader(
                         new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            return output.toString();

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("FFprobe capture failed: " + e.getMessage(), e);
        }
    }
    /*
     * readOutput() reads the console output of FFmpeg in a separate thread.
     * This is very important because FFmpeg continuously writes logs while processing.
     * If we do not read the output the buffer fills up and FFmpeg freezes waiting
     * for the buffer to be emptied which causes a deadlock and the app hangs forever.
     * By reading in a separate thread the buffer is always emptied continuously
     * so FFmpeg keeps running without any freeze or deadlock issue.
     * Every line printed by FFmpeg like frame count and progress is shown in console.
     */
    private void readOutput(InputStream inputStream) {
        new Thread(() -> {
            try (BufferedReader reader =
                     new BufferedReader(
                         new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[FFmpeg] " + line);
                }
            } catch (IOException e) {
                System.err.println("Error reading FFmpeg output: "
                        + e.getMessage());
            }
        }).start();
    }
}
//```
//
//---
//
//## Steps
//```
//1. Replace entire FFmpegExecutor.java with above code
//2. Save file
//3. Eclipse will auto restart (DevTools)
//4. Test in Postman again