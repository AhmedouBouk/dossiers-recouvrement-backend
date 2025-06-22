package com.bnm.recouvrement.service;

import com.bnm.recouvrement.entity.LcFile;
import com.bnm.recouvrement.repository.LcFileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class LcFileService {

    private static final Logger logger = LoggerFactory.getLogger(LcFileService.class);
    private final LcFileRepository lcFileRepository;
    private final FileStorageService fileStorageService;

    public LcFileService(LcFileRepository lcFileRepository, FileStorageService fileStorageService) {
        this.lcFileRepository = lcFileRepository;
        this.fileStorageService = fileStorageService;
    }

    public LcFile saveLcFile(LcFile lcFile, MultipartFile file, String createdBy) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filePath = fileStorageService.storeFile(file, "lc_files");
            lcFile.setFilePath(filePath);
        }
        lcFile.setUploadDate(LocalDateTime.now());
        lcFile.setCreatedBy(createdBy);
        return lcFileRepository.save(lcFile);
    }

    public List<LcFile> getLcFilesByDossierId(Long dossierId) {
        return lcFileRepository.findByDossierRecouvrementId(dossierId);
    }

    public Optional<LcFile> getLcFileById(Long id) {
        return lcFileRepository.findById(id);
    }

    public LcFile updateLcFile(Long id, LcFile updatedLcFile, MultipartFile file, String updatedBy) throws IOException {
        Optional<LcFile> existingLcFileOpt = lcFileRepository.findById(id);
        if (existingLcFileOpt.isPresent()) {
            LcFile existingLcFile = existingLcFileOpt.get();
            existingLcFile.setTitle(updatedLcFile.getTitle());
            existingLcFile.setLcNumber(updatedLcFile.getLcNumber());
            existingLcFile.setMontant(updatedLcFile.getMontant());
            existingLcFile.setDateEcheance(updatedLcFile.getDateEcheance());
            if (file != null && !file.isEmpty()) {
                String filePath = fileStorageService.storeFile(file, "lc_files");
                existingLcFile.setFilePath(filePath);
            }
            existingLcFile.setUpdatedAt(LocalDateTime.now());
            existingLcFile.setUpdatedBy(updatedBy);
            return lcFileRepository.save(existingLcFile);
        }
        return null;
    }

    public boolean deleteLcFile(Long id) {
        Optional<LcFile> lcFileOpt = lcFileRepository.findById(id);
        if (lcFileOpt.isPresent()) {
            LcFile lcFile = lcFileOpt.get();
            String filePath = lcFile.getFilePath();
            if (filePath != null) {
                try {
                    Files.deleteIfExists(Paths.get(filePath));
                } catch (IOException e) {
                    logger.error("Error deleting file: {}", e.getMessage());
                }
            }
            lcFileRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public byte[] getLcFileContent(Long id) throws IOException {
        Optional<LcFile> lcFileOptional = lcFileRepository.findById(id);
        if (lcFileOptional.isPresent()) {
            LcFile lcFile = lcFileOptional.get();
            if (lcFile.getFilePath() != null) {
                return Files.readAllBytes(Paths.get(lcFile.getFilePath()));
            }
        }
        logger.warn("LC file content not found for id: {}", id);
        throw new IOException("LC file not found");
    }
}