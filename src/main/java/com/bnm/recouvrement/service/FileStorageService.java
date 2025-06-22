package com.bnm.recouvrement.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads/";

    public String storeFile(MultipartFile file, String subDirectory) throws IOException {
        String uploadDir = UPLOAD_DIR + subDirectory;
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        return filePath.toString();
    }

    public void deleteFile(String filePath) throws IOException {
        if (filePath != null) {
            Path path = Paths.get(filePath);
            Files.deleteIfExists(path);
        }
    }
}
