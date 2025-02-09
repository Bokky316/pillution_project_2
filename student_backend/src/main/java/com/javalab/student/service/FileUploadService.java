package com.javalab.student.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileUploadService {

    private static final String UPLOAD_DIR = "src/main/resources/static/images/"; // 이미지 저장 경로

    public String uploadFile(MultipartFile file) {
        try {
            // 1. 파일 이름 생성 (UUID 사용)
            String originalFilename = file.getOriginalFilename();
            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID().toString() + fileExtension;

            // 2. 파일 저장 경로 생성
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // 3. 파일 저장
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // 4. 이미지 URL 생성 (예: /images/UUID.jpg)
            String imageUrl = "/images/" + newFilename;

            return imageUrl;

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("이미지 업로드 실패");
        }
    }
}
