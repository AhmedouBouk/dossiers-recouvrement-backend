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
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class CreditsService {

    private final CreditFileRepository creditFileRepository;
    private final DossierRecouvrementRepository dossierRepository;
    private final HistoryService historyService;
    
    private static final String UPLOAD_DIR = "uploads/credit_files/";
    private static final String NOT_FOUND_MESSAGE = "CreditFile not found with id: ";
    
    public CreditsService(CreditFileRepository creditFileRepository, DossierRecouvrementRepository dossierRepository, HistoryService historyService) {
        this.creditFileRepository = creditFileRepository;
        this.dossierRepository = dossierRepository;
        this.historyService = historyService;
    }

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

        CreditFile savedFile = creditFileRepository.save(creditFile);
        
        // Record the event in history
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        historyService.createEvent(
            username,
            "upload", 
            "credit", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Téléchargement du fichier crédit: " + title
        );
        
        return savedFile;
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
        
        // Record the event in history
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        historyService.createEvent(
            username,
            "delete", 
            "credit", 
            creditFile.getDossier().getId().toString(), 
            "Dossier #" + creditFile.getDossier().getId(),
            "Suppression du fichier crédit: " + creditFile.getTitle()
        );
    }

    public byte[] getCreditFileContent(Long id) throws IOException {
        CreditFile creditFile = creditFileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(NOT_FOUND_MESSAGE + id));

        if (creditFile.getFilePath() == null) {
            throw new RuntimeException("No file path associated with CreditFile id: " + id);
        }

        return Files.readAllBytes(Paths.get(creditFile.getFilePath()));
    }
    // Legacy methods for backward compatibility
    
    public DossierRecouvrement uploadCreditsFile(Long dossierId, MultipartFile file) throws IOException {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        // Save as a CreditFile and also maintain the legacy field
        saveCreditFile(dossierId, file.getOriginalFilename(), file, null, null, null);
        
        // Legacy path handling
        Path rootLocation = Paths.get("uploads/credits");
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path destinationFile = rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
        Files.copy(file.getInputStream(), destinationFile);
        
        String fileUrl = "/credits/" + fileName.replace(" ", "%20");
        dossier.setCreditsFile(fileUrl);
        
        return dossierRepository.save(dossier);
    }
    
    public void deleteCreditsFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        String oldFileUrl = dossier.getCreditsFile();
        dossier.setCreditsFile(null);
        dossierRepository.save(dossier);
        
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        historyService.createEvent(
            username,
            "delete", 
            "credit", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Suppression du fichier crédit: " + oldFileUrl
        );
    }
    
    public String getCreditsFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId).orElse(null);
        if (dossier != null && dossier.getCreditsFile() != null) {
            String baseUrl = "http://localhost:8080";
            return baseUrl + dossier.getCreditsFile();
        }
        return null;
    }
}