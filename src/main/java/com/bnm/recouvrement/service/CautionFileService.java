package com.bnm.recouvrement.service;

import com.bnm.recouvrement.repository.CautionFileRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.CautionFile;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CautionFileService {

    private final CautionFileRepository cautionFileRepository;
    private final DossierRecouvrementRepository dossierRepository;

    public CautionFileService(CautionFileRepository cautionFileRepository, DossierRecouvrementRepository dossierRepository) {
        this.cautionFileRepository = cautionFileRepository;
        this.dossierRepository = dossierRepository;
    }

    private static final String UPLOAD_DIR = "uploads/caution_files/";
    private static final String NOT_FOUND_MESSAGE = "CautionFile not found with id: ";

    public CautionFile saveCautionFile(Long dossierId, String title, MultipartFile file, String cautionNumber, Double montant, LocalDateTime dateEcheance) throws IOException {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found with id: " + dossierId));

        // Create upload directory if it doesn't exist
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // Save file to server
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(UPLOAD_DIR + fileName);
        Files.write(filePath, file.getBytes());

        // Create CautionFile entity
        CautionFile cautionFile = new CautionFile();
        cautionFile.setTitle(title);
        cautionFile.setFilePath(filePath.toString());
        cautionFile.setCautionNumber(cautionNumber);
        cautionFile.setMontant(montant);
        cautionFile.setDateEcheance(dateEcheance);
        cautionFile.setUploadDate(LocalDateTime.now());
        cautionFile.setDossier(dossier);

        return cautionFileRepository.save(cautionFile);
    }

    public Optional<CautionFile> getCautionFileById(Long id) {
        return cautionFileRepository.findById(id);
    }

    public List<CautionFile> getCautionFilesByDossierId(Long dossierId) {
        return cautionFileRepository.findByDossierId(dossierId);
    }

    public CautionFile updateCautionFile(Long id, String title, String cautionNumber, Double montant, LocalDateTime dateEcheance) {
        CautionFile cautionFile = cautionFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        cautionFile.setTitle(title);
        cautionFile.setCautionNumber(cautionNumber);
        cautionFile.setMontant(montant);
        cautionFile.setDateEcheance(dateEcheance);

        return cautionFileRepository.save(cautionFile);
    }

    public void deleteCautionFile(Long id) throws IOException {
        CautionFile cautionFile = cautionFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        // Delete file from server
        if (cautionFile.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(cautionFile.getFilePath()));
        }

        cautionFileRepository.deleteById(id);
    }

    public byte[] getCautionFileContent(Long id) throws IOException {
        CautionFile cautionFile = cautionFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        if (cautionFile.getFilePath() == null) {
            throw new RuntimeException("No file path associated with CautionFile id: " + id);
        }

        return Files.readAllBytes(Paths.get(cautionFile.getFilePath()));
    }
}
