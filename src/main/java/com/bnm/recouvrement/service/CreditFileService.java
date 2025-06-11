package com.bnm.recouvrement.service;

import com.bnm.recouvrement.repository.CreditFileRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.CreditFile;
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
public class CreditFileService {

    private final CreditFileRepository creditFileRepository;
    private final DossierRecouvrementRepository dossierRepository;

    public CreditFileService(CreditFileRepository creditFileRepository, DossierRecouvrementRepository dossierRepository) {
        this.creditFileRepository = creditFileRepository;
        this.dossierRepository = dossierRepository;
    }

    private static final String UPLOAD_DIR = "uploads/credit_files/";
    private static final String NOT_FOUND_MESSAGE = "CreditFile not found with id: ";

    public CreditFile saveCreditFile(Long dossierId, String title, MultipartFile file, String creditNumber, Double montant, LocalDateTime dateEcheance) throws IOException {
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

        // Create CreditFile entity
        CreditFile creditFile = new CreditFile();
        creditFile.setTitle(title);
        creditFile.setFilePath(filePath.toString());
        creditFile.setCreditNumber(creditNumber);
        creditFile.setMontant(montant);
        creditFile.setDateEcheance(dateEcheance);
        creditFile.setUploadDate(LocalDateTime.now());
        creditFile.setDossier(dossier);

        return creditFileRepository.save(creditFile);
    }

    public Optional<CreditFile> getCreditFileById(Long id) {
        return creditFileRepository.findById(id);
    }

    public List<CreditFile> getCreditFilesByDossierId(Long dossierId) {
        return creditFileRepository.findByDossierId(dossierId);
    }

    public CreditFile updateCreditFile(Long id, String title, String creditNumber, Double montant, LocalDateTime dateEcheance) {
        CreditFile creditFile = creditFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        creditFile.setTitle(title);
        creditFile.setCreditNumber(creditNumber);
        creditFile.setMontant(montant);
        creditFile.setDateEcheance(dateEcheance);

        return creditFileRepository.save(creditFile);
    }

    public void deleteCreditFile(Long id) throws IOException {
        CreditFile creditFile = creditFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        // Delete file from server
        if (creditFile.getFilePath() != null) {
            Files.deleteIfExists(Paths.get(creditFile.getFilePath()));
        }

        creditFileRepository.deleteById(id);
    }

    public byte[] getCreditFileContent(Long id) throws IOException {
        CreditFile creditFile = creditFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        if (creditFile.getFilePath() == null) {
            throw new RuntimeException("No file path associated with CreditFile id: " + id);
        }

        return Files.readAllBytes(Paths.get(creditFile.getFilePath()));
    }
}
