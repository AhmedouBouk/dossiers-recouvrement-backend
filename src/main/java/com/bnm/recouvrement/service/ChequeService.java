package com.bnm.recouvrement.service;

import org.springframework.stereotype.Service;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ChequeService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private HistoryService historyService;

    @Value("${server.port:8080}")
    private String serverPort;
    
    private final Path rootLocation = Paths.get("uploads/cheques");

    public DossierRecouvrement uploadChequeFile(Long dossierId, MultipartFile file) throws IOException {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
    
        // Créer le dossier de stockage s'il n'existe pas
        if (!Files.exists(rootLocation)) {
            Files.createDirectories(rootLocation);
        }
    
        // Générer un nom de fichier unique
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path destinationFile = rootLocation.resolve(Paths.get(fileName)).normalize().toAbsolutePath();
    
        // Sauvegarder le fichier sur le disque
        Files.copy(file.getInputStream(), destinationFile);
    
        // Important: Stocker seulement le chemin relatif dans la base de données
        String fileUrl = "/cheques/" + fileName;
        dossier.setChequeFile(fileUrl);
    
        DossierRecouvrement updatedDossier = dossierRepository.save(dossier);
    
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
    
        historyService.createEvent(
            username,
            "upload", 
            "cheque", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Téléchargement du fichier chèque: " + file.getOriginalFilename()
        );
    
        return updatedDossier;
    }

    public void deleteChequeFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
            .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        // Enregistrer l'ancien nom du fichier pour l'historique
        String oldFileUrl = dossier.getChequeFile();
        
        // Supprimer le fichier physique si nécessaire
        if (oldFileUrl != null && !oldFileUrl.isEmpty()) {
            try {
                String fileName = oldFileUrl.substring(oldFileUrl.lastIndexOf('/') + 1);
                Path filePath = rootLocation.resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Logger l'erreur mais continuer le processus
                System.err.println("Impossible de supprimer le fichier: " + e.getMessage());
            }
        }
        
        dossier.setChequeFile(null);
        dossierRepository.save(dossier);
        
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.createEvent(
            username,
            "delete", 
            "cheque", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Suppression du fichier chèque: " + oldFileUrl
        );
    }

    public String getChequeFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId).orElse(null);
        if (dossier != null && dossier.getChequeFile() != null) {
            // Retourne l'URL relative, sera convertie en URL absolue par le frontend si nécessaire
            return dossier.getChequeFile();
        }
        return null;
    }
}