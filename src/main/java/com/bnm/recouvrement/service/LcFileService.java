package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LcFileService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    private final Path rootLocation = Paths.get("uploads/lc-files");

    // Uploader un fichier LC
    public DossierRecouvrement uploadLcFile(Long dossierId, MultipartFile file) throws IOException {
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
        String fileUrl = "http://localhost:8080/lc-files/" + fileName.replace(" ", "%20"); // Encoder les espaces
        dossier.setLcFile(fileUrl);

        return dossierRepository.save(dossier);
    }

    // Supprimer un fichier LC
    public void deleteLcFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));
        dossier.setLcFile(null);
        dossierRepository.save(dossier);
    }

    // Récupérer l'URL du fichier LC
    public String getLcFile(Long dossierId) {
        DossierRecouvrement dossier = dossierRepository.findById(dossierId).orElse(null);
        if (dossier != null && dossier.getLcFile() != null) {
            // Construire l'URL HTTP pour le fichier
            String baseUrl = "http://localhost:8080";
            return baseUrl + dossier.getLcFile();
        }
        return null;
    }
}