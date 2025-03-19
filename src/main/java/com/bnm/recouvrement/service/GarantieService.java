package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class GarantieService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private HistoryService historyService;

    private final Path rootLocation = Paths.get("uploads/garanties"); // Dossier de stockage des fichiers de garantie

    // Uploader un fichier de garantie
    public DossierRecouvrement uploadGarantie(Long dossierId, MultipartFile file, String titre) throws IOException {
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
    
        // Enregistrer le chemin relatif du fichier dans la base de données
        String fileUrl = "/garanties/" + fileName; // Chemin relatif
        dossier.setGarantiesTitre(titre);
        dossier.setGarantiesFile(fileUrl);
    
        DossierRecouvrement updatedDossier = dossierRepository.save(dossier);
    
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
    
        historyService.createEvent(
            username,
            "upload",
            "garantie",
            dossierId.toString(),
            "Dossier #" + dossierId,
            "Téléchargement du fichier garantie '" + titre + "': " + file.getOriginalFilename()
        );
    
        return updatedDossier;
    }
    public DossierRecouvrement updateGarantie(Long id, DossierRecouvrement garantieDetails) {
        return dossierRepository.findById(id)
            .map(dossier -> {
                dossier.setGarantiesTitre(garantieDetails.getGarantiesTitre());
                dossier.setGarantiesFile(garantieDetails.getGarantiesFile());
                return dossierRepository.save(dossier);
            })
            .orElseThrow(() -> new RuntimeException("Garantie non trouvée avec l'ID : " + id));
    }
    // Récupérer l'URL du fichier de garantie
    public String getGarantieFileUrl(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId).orElse(null);
        if (dossier != null && dossier.getGarantiesFile() != null) {
            return dossier.getGarantiesFile(); // Retourne le chemin relatif
        }
        return null;
    }

    // Supprimer un fichier de garantie
    public void deleteGarantieFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        // Supprimer le fichier physique si nécessaire
        if (dossier.getGarantiesFile() != null) {
            try {
                String fileName = dossier.getGarantiesFile().substring(dossier.getGarantiesFile().lastIndexOf('/') + 1);
                Path filePath = rootLocation.resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Logger l'erreur mais continuer le processus
                System.err.println("Impossible de supprimer le fichier: " + e.getMessage());
            }
        }

        // Réinitialiser le champ garantiesFile dans la base de données
        dossier.setGarantiesFile(null);
        dossierRepository.save(dossier);

        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        historyService.createEvent(
            username,
            "delete",
            "garantie",
            dossierId.toString(),
            "Dossier #" + dossierId,
            "Suppression du fichier garantie"
        );
    }
}