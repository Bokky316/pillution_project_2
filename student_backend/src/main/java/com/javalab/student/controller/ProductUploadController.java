package com.javalab.student.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductUploadController {

    private final Path uploadPath;

    // 생성자에서 properties에 설정된 uploadPath 주입
    public ProductUploadController(@Value("${uploadPath}") String uploadPathStr) throws IOException {
        // "file://" 접두사를 제거
        String processedPath = uploadPathStr.replace("file://", "");
        // Windows 환경에서 "/c:/shop/"와 같이 앞에 불필요한 슬래시가 붙을 경우 이를 제거
        if (processedPath.startsWith("/") && processedPath.charAt(2) == ':') {
            processedPath = processedPath.substring(1);
        }
        this.uploadPath = Paths.get(processedPath);
        if (!Files.exists(this.uploadPath)) {
            Files.createDirectories(this.uploadPath);
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadImage(@RequestPart("imageFile") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new UploadResponse("파일이 비어있습니다."));
        }
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            String fileUrl = "http://localhost:8080/images/" + fileName;
            return ResponseEntity.ok(new UploadResponse(fileUrl));
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new UploadResponse("파일 저장 실패: " + ex.getMessage()));
        }
    }

    // 간단한 DTO: 업로드 결과(이미지 URL 또는 에러 메시지)를 전달하는 역할
    public static class UploadResponse {
        private String imageUrl;

        public UploadResponse(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }
    }
}