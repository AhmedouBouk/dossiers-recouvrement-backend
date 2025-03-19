package com.bnm.recouvrement.service;

import org.springframework.stereotype.Service;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class CautionsService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private HistoryService historyService;

    private final Path rootLocation = Paths.get("uploads/cautions");

    public DossierRecouvrement uploadCautionsFile(Long dossierId, MultipartFile file) throws IOException {
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

        // Enregistrer l'URL du fichier dans la base de données
        String fileUrl = "/cautions/" + fileName.replace(" ", "%20"); // Chemin relatif        dossier.setCautionsFile(fileUrl);
        
        DossierRecouvrement updatedDossier = dossierRepository.save(dossier);
        
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.createEvent(
            username,
            "upload", 
            "caution", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Téléchargement du fichier caution: " + file.getOriginalFilename()
        );

        return updatedDossier;
    }

    public void deleteCautionsFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        
        // Enregistrer l'ancien nom du fichier pour l'historique
        String oldFileUrl = dossier.getCautionsFile();
        
        dossier.setCautionsFile(null);
        dossierRepository.save(dossier);
        
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.createEvent(
            username,
            "delete", 
            "caution", 
            dossierId.toString(), 
            "Dossier #" + dossierId,
            "Suppression du fichier caution: " + oldFileUrl
        );
    }

  
    public String getCautionsFile(Long dossierId) {
        return dossierRepository.findById(dossierId)
            .map(dossier -> {
                if (dossier.getCautionsFile() != null && !dossier.getCautionsFile().isEmpty()) {
                    String baseUrl = "http://localhost:8080/files"; // Remplace par ton vrai endpoint
                    String filePath = dossier.getCautionsFile();

                    // Éviter les doubles "/"
                    if (filePath.startsWith("/")) {
                        filePath = filePath.substring(1);
                    }

                    return baseUrl + "/" + filePath;
                }
                return null;
            })
            .orElse(null);
    }

}