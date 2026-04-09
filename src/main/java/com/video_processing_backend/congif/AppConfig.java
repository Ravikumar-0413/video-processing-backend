 package com.video_processing_backend.congif;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;
/*
 * AppConfig is the configuration class of the entire application.
 * It reads all the settings from application.properties file using @Value annotation
 * and provides them to other classes like FFmpegExecutor and Service classes.
 * It also configures CORS so that frontend applications can call our backend APIs
 * and configures MultipartResolver so that file uploads work correctly.
 * Any class that needs FFmpeg paths or temp folder paths will get them from this class.
 */
@Configuration
public class AppConfig implements WebMvcConfigurer {

	/*
     * @Value annotation reads the value from application.properties file.
     * inputDir  - reads app.temp.input-dir  which is temp/input
     * outputDir - reads app.temp.output-dir which is temp/output
     * ffmpegPath  - reads app.ffmpeg.path  which is C:/ffmpeg/bin/ffmpeg.exe
     * ffprobePath - reads app.ffprobe.path which is C:/ffmpeg/bin/ffprobe.exe
     */
    @Value("${app.temp.input-dir}")
    private String inputDir;

    @Value("${app.temp.output-dir}")
    private String outputDir;

    @Value("${app.ffmpeg.path}")
    private String ffmpegPath;

    @Value("${app.ffprobe.path}")
    private String ffprobePath;

    /*
     * CORS stands for Cross Origin Resource Sharing.
     * By default browsers block requests coming from a different domain or port.
     * For example if frontend runs on localhost:3000 and backend runs on localhost:8080
     * the browser will block the request without CORS configuration.
     * This method allows all origins, all headers and all HTTP methods like
     * GET, POST, PUT, DELETE and OPTIONS to call our backend APIs.
     * The mapping /api/** means CORS is enabled for all our API endpoints.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods(
                        "GET",
                        "POST",
                        "PUT",
                        "DELETE",
                        "OPTIONS"
                )
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /*
     * MultipartResolver is required to handle file upload requests.
     * When a user sends a video file through Postman or a frontend application
     * Spring Boot uses this resolver to read and process the uploaded file.
     * Without this bean file uploads will not work correctly.
     */
    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    /*
     * getInputDir returns the absolute path of the temp/input folder.
     * It converts the relative path from application.properties to an absolute path
     * so FFmpeg can locate the folder correctly on any computer.
     * If the folder does not exist for any reason it creates it automatically
     * before returning the path so file saving never fails.
     */
    public String getInputDir() {
        File dir = new File(inputDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("✅ Created input dir: "
                    + dir.getAbsolutePath());
        }
        return dir.getAbsolutePath();
    }
    /*
     * getOutputDir returns the absolute path of the temp/output folder.
     * It converts the relative path from application.properties to an absolute path
     * so FFmpeg can save the processed file correctly on any computer.
     * If the folder does not exist for any reason it creates it automatically
     * before returning the path so file saving never fails.
     */
    public String getOutputDir() {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("✅ Created output dir: "
                    + dir.getAbsolutePath());
        }
        return dir.getAbsolutePath();
    }
    /*
     * getFfmpegPath returns the path of ffmpeg.exe from application.properties.
     * This path is used by FFmpegExecutor to run all video processing commands
     * like trim, crop, compress, convert and audio extraction.
     */
    public String getFfmpegPath() {
        return ffmpegPath;
    }
    /*
     * getFfprobePath returns the path of ffprobe.exe from application.properties.
     * FFprobe is a companion tool of FFmpeg that is used to read
     * video information like duration, resolution and codec details.
     */
    public String getFfprobePath() {
        return ffprobePath;
    }
}
 