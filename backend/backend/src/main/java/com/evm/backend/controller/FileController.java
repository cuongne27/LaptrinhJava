// controller/FileController.java
package com.evm.backend.controller;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    // Thư mục lưu file (có thể config trong application.properties)
    private final String UPLOAD_DIR = "uploads/images/";

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        FileUploadResponse.builder()
                                .success(false)
                                .message("File is empty")
                                .build()
                );
            }

            // Validate file type (chỉ cho phép ảnh)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(
                        FileUploadResponse.builder()
                                .success(false)
                                .message("Only image files are allowed")
                                .build()
                );
            }

            // Tạo tên file unique
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // Tạo thư mục nếu chưa tồn tại
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Lưu file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            // Tạo URL trả về
            String fileUrl = "/images/" + filename;

            log.info("File uploaded successfully: {}", filename);

            return ResponseEntity.ok(
                    FileUploadResponse.builder()
                            .success(true)
                            .message("File uploaded successfully")
                            .url(fileUrl)
                            .filename(filename)
                            .build()
            );

        } catch (IOException e) {
            log.error("Error uploading file", e);
            return ResponseEntity.internalServerError().body(
                    FileUploadResponse.builder()
                            .success(false)
                            .message("Failed to upload file: " + e.getMessage())
                            .build()
            );
        }
    }

    @Data
    @Builder
    public static class FileUploadResponse {
        private boolean success;
        private String message;
        private String url;
        private String filename;
    }
}