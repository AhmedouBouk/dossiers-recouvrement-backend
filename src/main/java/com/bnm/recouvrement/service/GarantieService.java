package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.dao.GarantieRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.Garantie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class GarantieService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private GarantieRepository garantieRepository;

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
        
        // Créer une nouvelle garantie
        Garantie garantie = new Garantie();
        garantie.setTitre(titre);
        garantie.setFilePath(fileUrl);
        garantie.setUploadDate(LocalDateTime.now());
        garantie.setDossier(dossier);
        
        // Ajouter la garantie au dossier et sauvegarder
        dossier.getGaranties().add(garantie);
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
    public Garantie updateGarantie(Long garantieId, String nouveauTitre) {
        return garantieRepository.findById(garantieId)
            .map(garantie -> {
                garantie.setTitre(nouveauTitre);
                return garantieRepository.save(garantie);
            })
            .orElseThrow(() -> new RuntimeException("Garantie non trouvée avec l'ID : " + garantieId));
    }
    // Récupérer toutes les garanties d'un dossier
    public List<Garantie> getGarantiesByDossierId(Long dossierId) {
        return garantieRepository.findByDossierId(dossierId);
    }
    
    // Récupérer une garantie par son ID
    public Garantie getGarantieById(Long garantieId) {
        return garantieRepository.findById(garantieId)
            .orElseThrow(() -> new RuntimeException("Garantie non trouvée avec l'ID : " + garantieId));
    }

    // Supprimer une garantie
    public void deleteGarantie(Long garantieId) {
        Garantie garantie = garantieRepository.findById(garantieId)
                .orElseThrow(() -> new RuntimeException("Garantie non trouvée"));
        
        Long dossierId = garantie.getDossier().getId();
        String titre = garantie.getTitre();

        // Supprimer le fichier physique
        try {
            String fileName = garantie.getFilePath().substring(garantie.getFilePath().lastIndexOf('/') + 1);
            Path filePath = rootLocation.resolve(fileName);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Logger l'erreur mais continuer le processus
            System.err.println("Impossible de supprimer le fichier: " + e.getMessage());
        }

        // Supprimer la garantie de la base de données
        garantieRepository.deleteById(garantieId);

        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        historyService.createEvent(
            username,
            "delete",
            "garantie",
            dossierId.toString(),
            "Dossier #" + dossierId,
            "Suppression de la garantie '" + titre + "'"
        );
    }
    
    // Supprimer toutes les garanties d'un dossier
    public void deleteAllGarantiesByDossierId(Long dossierId) {
        List<Garantie> garanties = garantieRepository.findByDossierId(dossierId);
        
        // Supprimer les fichiers physiques
        for (Garantie garantie : garanties) {
            try {
                String fileName = garantie.getFilePath().substring(garantie.getFilePath().lastIndexOf('/') + 1);
                Path filePath = rootLocation.resolve(fileName);
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                System.err.println("Impossible de supprimer le fichier: " + e.getMessage());
            }
        }
        
        // Supprimer toutes les garanties de la base de données
        garantieRepository.deleteByDossierId(dossierId);
        
        // Enregistrer l'événement dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.createEvent(
            username,
            "delete",
            "garantie",
            dossierId.toString(),
            "Dossier #" + dossierId,
            "Suppression de toutes les garanties"
        );
    }
    
    // Récupérer le fichier PDF d'une garantie
    public byte[] getGarantiePdf(Long garantieId) throws IOException {
        Garantie garantie = garantieRepository.findById(garantieId)
                .orElseThrow(() -> new RuntimeException("Garantie non trouvée"));
        
        String filePath = garantie.getFilePath();
        if (filePath == null || filePath.isBlank()) {
            throw new RuntimeException("Fichier non trouvé");
        }
        
        String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
        Path path = rootLocation.resolve(fileName);
        
        return Files.readAllBytes(path);
    }
}