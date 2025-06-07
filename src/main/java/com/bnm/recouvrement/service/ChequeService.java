package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.ChequeFileRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.ChequeFile;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // Will be removed after constructor injection
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ChequeService {

    private static final Logger logger = LoggerFactory.getLogger(ChequeService.class);
    private static final String UPLOADS_CHEQUES_PATH = "uploads/cheques";
    private static final String USER_SYSTEM = "system";
    private static final String EVENT_TYPE_CHEQUE_FILE = "cheque_file";
    private static final String LOG_MSG_DOSSIER_PREFIX = "Dossier #";
    private static final String LOG_MSG_CHEQUE_FILE_PREFIX = " - Cheque File #";
    private static final String ERROR_MSG_CANNOT_DELETE_PHYSICAL_FILE = "Impossible de supprimer le fichier physique: {} Erreur: {}";

    private final DossierRecouvrementRepository dossierRepository;
    private final ChequeFileRepository chequeFileRepository;
    private final HistoryService historyService;
    private final Path rootLocation;

    @Autowired
    public ChequeService(DossierRecouvrementRepository dossierRepository,
                         ChequeFileRepository chequeFileRepository,
                         HistoryService historyService) {
        this.dossierRepository = dossierRepository;
        this.chequeFileRepository = chequeFileRepository;
        this.historyService = historyService;
        this.rootLocation = Paths.get(UPLOADS_CHEQUES_PATH);
    }

    @Transactional
    public ChequeFile uploadChequeFile(Long dossierId, String title, String chequeNumber, Double montant, 
                                       LocalDateTime dateEcheance, MultipartFile file) throws IOException {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé avec ID: " + dossierId));

        try {
            if (!Files.exists(rootLocation)) {
                Files.createDirectories(rootLocation);
            }
        } catch (IOException e) {
            logger.error("Could not create storage directory: {}", rootLocation, e);
            throw new RuntimeException("Could not create storage directory", e);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            originalFilename = "file"; // Default filename if null or empty
        }
        String fileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
        Path destinationFile = rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();

        Files.copy(file.getInputStream(), destinationFile);

        String relativeFilePath = "/cheques/" + fileName;

        ChequeFile chequeFile = new ChequeFile();
        chequeFile.setTitle(title);
        chequeFile.setFilePath(relativeFilePath);
        chequeFile.setChequeNumber(chequeNumber);
        chequeFile.setMontant(montant);
        chequeFile.setDateEcheance(dateEcheance);
        chequeFile.setUploadDate(LocalDateTime.now());
        chequeFile.setDossier(dossier);

        ChequeFile savedChequeFile = chequeFileRepository.save(chequeFile);

        // History event
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.getName() != null ? auth.getName() : USER_SYSTEM;
        historyService.createEvent(
                username,
                "upload",
                EVENT_TYPE_CHEQUE_FILE,
                savedChequeFile.getId().toString(),
                LOG_MSG_DOSSIER_PREFIX + dossierId + LOG_MSG_CHEQUE_FILE_PREFIX + savedChequeFile.getId(),
                "Téléchargement du fichier chèque: " + originalFilename + " avec titre: " + title
        );

        return savedChequeFile;
    }

    public Optional<ChequeFile> getChequeFileById(Long chequeFileId) {
        return chequeFileRepository.findById(chequeFileId);
    }

    public List<ChequeFile> getChequeFilesByDossierId(Long dossierId) {
        return chequeFileRepository.findByDossierId(dossierId);
    }

    @Transactional
    public ChequeFile updateChequeFile(Long chequeFileId, String title, String chequeNumber, Double montant, LocalDateTime dateEcheance) {
        ChequeFile chequeFile = chequeFileRepository.findById(chequeFileId)
                .orElseThrow(() -> new RuntimeException("Fichier chèque non trouvé avec ID: " + chequeFileId));

        chequeFile.setTitle(title);
        chequeFile.setChequeNumber(chequeNumber);
        chequeFile.setMontant(montant);
        chequeFile.setDateEcheance(dateEcheance);
        // filePath and uploadDate are not typically updated here

        ChequeFile updatedChequeFile = chequeFileRepository.save(chequeFile);

        // History event
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.getName() != null ? auth.getName() : USER_SYSTEM;
        historyService.createEvent(
                username,
                "update",
                EVENT_TYPE_CHEQUE_FILE,
                chequeFileId.toString(),
                LOG_MSG_DOSSIER_PREFIX + chequeFile.getDossier().getId() + LOG_MSG_CHEQUE_FILE_PREFIX + chequeFileId,
                "Mise à jour des métadonnées du fichier chèque: " + chequeFile.getFilePath()
        );

        return updatedChequeFile;
    }

    @Transactional
    public void deleteChequeFile(Long chequeFileId) throws IOException {
        ChequeFile chequeFile = chequeFileRepository.findById(chequeFileId)
                .orElseThrow(() -> new RuntimeException("Fichier chèque non trouvé avec ID: " + chequeFileId));

        String filePathStr = chequeFile.getFilePath();
        if (filePathStr != null && !filePathStr.isEmpty()) {
            try {
                String fileName = filePathStr.substring(filePathStr.lastIndexOf('/') + 1);
                Path physicalFile = rootLocation.resolve(fileName);
                Files.deleteIfExists(physicalFile);
            } catch (IOException e) {
                logger.error(ERROR_MSG_CANNOT_DELETE_PHYSICAL_FILE, filePathStr, e.getMessage());
                // Decide if to re-throw or just log. For now, log and continue to delete DB record.
            }
        }

        Long dossierId = chequeFile.getDossier().getId();
        chequeFileRepository.delete(chequeFile);

        // History event
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.getName() != null ? auth.getName() : USER_SYSTEM;
        historyService.createEvent(
                username,
                "delete",
                EVENT_TYPE_CHEQUE_FILE,
                chequeFileId.toString(),
                LOG_MSG_DOSSIER_PREFIX + dossierId + LOG_MSG_CHEQUE_FILE_PREFIX + chequeFileId,
                "Suppression du fichier chèque: " + filePathStr
        );
    }

    @Transactional
    public void deleteAllChequeFilesByDossierId(Long dossierId) throws IOException {
        List<ChequeFile> chequeFiles = chequeFileRepository.findByDossierId(dossierId);
        if (chequeFiles.isEmpty()) {
            return; // No files to delete
        }

        for (ChequeFile chequeFile : chequeFiles) {
            String filePathStr = chequeFile.getFilePath();
            if (filePathStr != null && !filePathStr.isEmpty()) {
                try {
                    String fileName = filePathStr.substring(filePathStr.lastIndexOf('/') + 1);
                    Path physicalFile = rootLocation.resolve(fileName);
                    Files.deleteIfExists(physicalFile);
                } catch (IOException e) {
                    logger.error(ERROR_MSG_CANNOT_DELETE_PHYSICAL_FILE, filePathStr, e.getMessage());
                    // Continue deleting other files and DB records
                }
            }
        }

        chequeFileRepository.deleteByDossierId(dossierId); // Efficiently delete all by dossierId

        // History event
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null && auth.getName() != null ? auth.getName() : USER_SYSTEM;
        historyService.createEvent(
                username,
                "delete_all",
                EVENT_TYPE_CHEQUE_FILE,
                dossierId.toString(),
                LOG_MSG_DOSSIER_PREFIX + dossierId,
                "Suppression de tous les fichiers chèques pour le dossier."
        );
    }
}