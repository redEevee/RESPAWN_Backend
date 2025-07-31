package com.shop.respawn.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ImageService {

    @Value("${uploadPath}")
    private String uploadPathKey;

    public String saveImage(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadPathKey); // 또는 상대경로 "uploads"
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path savePath = Paths.get(uploadPathKey, fileName);
        Files.copy(file.getInputStream(), savePath);

        // 예시: 서버가 http://localhost:8080/uploads/ 를 static으로 제공한다고 가정
        return "/uploads/" + fileName;
    }
}
