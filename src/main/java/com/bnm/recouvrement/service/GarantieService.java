package com.bnm.recouvrement.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
public class GarantieService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    private final Path rootLocation = Paths.get("uploads"); // Dossier de stockage des fichiers

    public List<DossierRecouvrement> getDossiersSansGarantie() {
        return dossierRepository.findByGarantiesFileIsNull(); // Recherche les dossiers sans garantie
    }

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

        // Enregistrer le chemin du fichier dans la base de données
        dossier.setGarantiesTitre(titre);
        dossier.setGarantiesFile(destinationFile.toString()); // Stocke le chemin du fichier

        return dossierRepository.save(dossier);
    }
}