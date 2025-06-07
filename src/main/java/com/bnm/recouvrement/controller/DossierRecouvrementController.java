package com.bnm.recouvrement.controller;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import java.util.HashMap;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;

import com.bnm.recouvrement.entity.Agence;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.Garantie;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.service.ChequeService;
import com.bnm.recouvrement.service.DossierRecouvrementService;
import org.springframework.core.io.Resource;import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
@RestController
@RequestMapping("/dossiers")
@CrossOrigin(origins = "http://localhost:4200")
public class DossierRecouvrementController {

    @Autowired
    private DossierRecouvrementService dossierService;
    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;
    @GetMapping("/affichage")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DossierRecouvrement>> getAllDossiers() {
        try {
            // Récupérer l'utilisateur actuellement authentifié
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal(); // Cast vers votre classe User
    
            // Récupérer le type d'utilisateur et l'agence
            String userType = user.getUserType();
    
            Agence agence = user.getAgence();

            String code = (agence != null) ? agence.getCode(): null;

            // Récupérer tous les dossiers
            List<DossierRecouvrement> dossiers = dossierService.getAllDossiers();
    
            // Filtrer les dossiers en fonction du type d'utilisateur
            List<DossierRecouvrement> filteredDossiers;

           if ("Do".equals(userType)) {
                // Afficher uniquement les dossiers dont les champs referencesCredits, referencesCautions, referencesLC ne sont pas null
                filteredDossiers = dossiers.stream()
                    .filter(dossier -> dossier.getReferencesCredits() != null &&
                                      dossier.getReferencesCautions() != null &&
                                      dossier.getReferencesLC() != null)
                    .collect(Collectors.toList());
            } else if ("Agence".equals(userType)) {
              


                filteredDossiers = dossiers.stream()
                .filter(dossier -> dossier.getReferencesChecks() != null && 
                                   dossier.getReferencesChecks().matches(".*/" + code + "/.*"))
                .collect(Collectors.toList());

            } else {
                // Si l'utilisateur n'est ni RECOUVREMENT, ni D, ni AGENCE, retourner tous les dossiers
                filteredDossiers = dossiers;
            }
    
            return ResponseEntity.ok(filteredDossiers);
        } catch (Exception e) {
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> getDossierById(@PathVariable Long id) {
        Optional<DossierRecouvrement> dossier = dossierService.getDossierById(id);
        return dossier.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> updateDossier(
            @PathVariable Long id,
            @RequestBody DossierRecouvrement dossier) {
        try {
            DossierRecouvrement updatedDossier = dossierService.updateDossier(id, dossier);
            return ResponseEntity.ok(updatedDossier);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> deleteDossier(@PathVariable Long id) {
        try {
            dossierService.deleteDossier(id);
            return ResponseEntity.ok("Dossier supprimé avec succès");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/recherche")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DossierRecouvrement>> searchDossiers(
            @RequestParam(required = false) Long dossierId,
            @RequestParam(required = false) String numeroCompte,
            @RequestParam(required = false) String nomClient) {
        try {
            List<DossierRecouvrement> dossiers = dossierService.searchDossiers(dossierId, numeroCompte, nomClient);
            if (!dossiers.isEmpty()) {
                return ResponseEntity.ok(dossiers);
            }
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DossierRecouvrement> createDossier(@RequestBody DossierRecouvrement dossier) {
        try {
            DossierRecouvrement newDossier = dossierService.createDossier(dossier);
            return ResponseEntity.status(HttpStatus.CREATED).body(newDossier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> importDossiers(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Fichier vide", "status", "error"));
            }

            String fileName = file.getOriginalFilename();
            if (fileName == null || !fileName.endsWith(".csv")) {
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "Le fichier doit être au format CSV", "status", "error"));
            }

            String filePath = System.getProperty("java.io.tmpdir") + "/" + fileName;
            File tempFile = new File(filePath);
            file.transferTo(tempFile);

            int importCount = dossierService.importDossiersFromFile(filePath);
            tempFile.delete();

            return ResponseEntity.ok(Map.of(
                "message", "Import réussi",
                "count", importCount,
                "status", "success"
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "message", "Erreur d'importation: " + e.getMessage(),
                    "status", "error"
                ));
        }
    }
     private final String uploadsBasePath = "uploads"; // Modifier selon votre configuration

    @GetMapping("/test")
    public String testRoute() {
        return "✅ Fusion Controller OK";
    }
 

@PostMapping(value = "/{id}/fusionner-complet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ResponseEntity<byte[]> fusionnerTout(
        @PathVariable Long id,
        @RequestParam("detailsPdf") MultipartFile detailsPdf,
        @RequestParam("miseEnDemeurePdf") MultipartFile miseEnDemeurePdf
) throws IOException {
    System.out.println("🚀 Début de la fusion flexible pour le dossier: " + id);
    
    // Récupérer les chemins des fichiers backend depuis la base
    DossierRecouvrement dossier = dossierRecouvrementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

    // Récupérer tous les chemins (peuvent être null)
    // Pour les garanties, on utilise maintenant la liste des garanties
    String creditPath = dossier.getCreditsFile();
    String cautionPath = dossier.getCautionsFile();
    String chequePath = dossier.getChequeFile();
    String lcPath = dossier.getLcFile();

    // Démarrer la fusion - TOUJOURS avec les PDFs frontend
    PDFMergerUtility merger = new PDFMergerUtility();
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    merger.setDestinationStream(outputStream);

    System.out.println("🔁 Fusion des fichiers frontend obligatoires...");
    merger.addSource(detailsPdf.getInputStream());
    merger.addSource(miseEnDemeurePdf.getInputStream());

    // Compteur pour suivre combien de fichiers backend ont été ajoutés
    int fichersBackendAjoutes = 0;

    // Tenter d'ajouter chaque fichier backend s'il existe
    // Pour les garanties, on ajoute tous les fichiers de garantie disponibles
    for (Garantie garantie : dossier.getGaranties()) {
        fichersBackendAjoutes += ajouterFichierSiExiste(merger, garantie.getFilePath(), "Garantie: " + garantie.getTitre());
    }
    fichersBackendAjoutes += ajouterFichierSiExiste(merger, creditPath, "Crédits");
    fichersBackendAjoutes += ajouterFichierSiExiste(merger, cautionPath, "Cautions");
    fichersBackendAjoutes += ajouterFichierSiExiste(merger, chequePath, "Chèque");
    fichersBackendAjoutes += ajouterFichierSiExiste(merger, lcPath, "LC");

    System.out.println("📊 Résumé: " + fichersBackendAjoutes + " fichiers backend ajoutés à la fusion");

    // Effectuer la fusion (même si aucun fichier backend n'a été ajouté)
    merger.mergeDocuments(null);

    System.out.println("✅ Fusion flexible réussie !");

    String filename = "fusion-complete-" + id + 
                     (fichersBackendAjoutes > 0 ? "-" + fichersBackendAjoutes + "files" : "-minimal") + 
                     ".pdf";

    return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .body(outputStream.toByteArray());
}

/**
 * Méthode utilitaire pour ajouter un fichier à la fusion s'il existe
 * @param merger L'objet PDFMergerUtility
 * @param relativePath Le chemin relatif du fichier
 * @param nomFichier Le nom du type de fichier pour les logs
 * @return 1 si le fichier a été ajouté, 0 sinon
 */
private int ajouterFichierSiExiste(PDFMergerUtility merger, String relativePath, String nomFichier) {
    try {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            System.out.println("⚠️ Chemin " + nomFichier + " non défini dans la base de données");
            return 0;
        }

        String absolutePath = convertToAbsolutePath(relativePath);
        File file = new File(absolutePath);

        if (!file.exists()) {
            System.out.println("⚠️ Fichier " + nomFichier + " inexistant: " + absolutePath);
            return 0;
        }

        if (!file.canRead()) {
            System.out.println("⚠️ Fichier " + nomFichier + " non lisible: " + absolutePath);
            return 0;
        }

        // Vérifier que c'est bien un PDF valide
        if (!estPdfValide(file)) {
            System.out.println("⚠️ Fichier " + nomFichier + " n'est pas un PDF valide: " + absolutePath);
            return 0;
        }

        merger.addSource(new FileInputStream(file));
        System.out.println("✅ Fichier " + nomFichier + " ajouté à la fusion: " + absolutePath);
        return 1;

    } catch (Exception e) {
        System.out.println("❌ Erreur lors de l'ajout du fichier " + nomFichier + ": " + e.getMessage());
        return 0;
    }
}

/**
 * Vérifie si un fichier est un PDF valide
 */
private boolean estPdfValide(File file) {
    try (PDDocument document = PDDocument.load(file)) {
        return document.getNumberOfPages() > 0;
    } catch (Exception e) {
        return false;
    }
}

/**
 * Convertit un chemin relatif en chemin absolu
 */
private String convertToAbsolutePath(String relativePath) {
    if (relativePath == null) return null;

    if (relativePath.startsWith("/")) {
        relativePath = relativePath.substring(1);
    }

    return uploadsBasePath + File.separator + relativePath;
}



@GetMapping("/{id}/check-files")
public ResponseEntity<Map<String, Boolean>> checkAvailableFiles(@PathVariable Long id) {
    try {
        DossierRecouvrement dossier = dossierRecouvrementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

        Map<String, Boolean> filesStatus = new HashMap<>();
        // Vérifier si le dossier a au moins une garantie
        filesStatus.put("garanties", !dossier.getGaranties().isEmpty());
        filesStatus.put("credits", fichierExiste(dossier.getCreditsFile()));
        filesStatus.put("cautions", fichierExiste(dossier.getCautionsFile()));
        filesStatus.put("cheque", fichierExiste(dossier.getChequeFile()));
        filesStatus.put("lc", fichierExiste(dossier.getLcFile()));
        
        // Calculer si on peut faire une fusion (au moins les PDFs frontend)
        filesStatus.put("canMerge", true); // Toujours true maintenant

        return ResponseEntity.ok(filesStatus);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

/**
 * Vérifie si un fichier existe à partir de son chemin relatif
 */
private boolean fichierExiste(String relativePath) {
    if (relativePath == null || relativePath.trim().isEmpty()) {
        return false;
    }
    
    try {
        String absolutePath = convertToAbsolutePath(relativePath);
        File file = new File(absolutePath);
        return file.exists() && file.canRead() && estPdfValide(file);
    } catch (Exception e) {
        return false;
    }
}



// Endpoints à ajouter/modifier dans votre Controller

/**
 * Récupère tous les dossiers actifs (non archivés)
 */
@GetMapping("/actifs")
public ResponseEntity<List<DossierRecouvrement>> getDossiersActifs() {
    return ResponseEntity.ok(dossierService.getDossiersActifs());
}

@GetMapping("/archives")
public ResponseEntity<List<DossierRecouvrement>> getDossiersArchives() {
    return ResponseEntity.ok(dossierService.getDossiersArchives());
}


/**
 * Archive un dossier
 */


/**
 * Désarchive un dossier
 */


/**
 * Compte le nombre de dossiers archivés
 */
@GetMapping("/archives/count")
public ResponseEntity<Map<String, Long>> countDossiersArchives() {
    try {
        long count = dossierService.countDossiersArchives();
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

/**
 * Vérifie si un dossier est archivé
 */
@GetMapping("/{id}/is-archived")
public ResponseEntity<Map<String, Boolean>> isDossierArchive(@PathVariable Long id) {
    try {
        boolean isArchived = dossierService.isDossierArchive(id);
        Map<String, Boolean> response = new HashMap<>();
        response.put("isArchived", isArchived);
        return ResponseEntity.ok(response);
    } catch (Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}


/**
 * Archive un dossier
 */
@PostMapping("/{id}/archiver")
public ResponseEntity<DossierRecouvrement> archiverDossier(@PathVariable Long id) {
    try {
        DossierRecouvrement dossier = dossierService.archiverDossier(id);
        return ResponseEntity.ok(dossier);
    } catch (IllegalStateException e) {
        // ✅ Retour 400 avec message d'erreur détaillé
        return ResponseEntity.badRequest()
            .header("Error-Message", e.getMessage())
            .build();
    } catch (RuntimeException e) {
        if (e.getMessage().contains("non trouvé")) {
            return ResponseEntity.notFound().build();
        }
        // ✅ Log l'erreur pour debugging
        System.err.println("Erreur archivage: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Error-Message", "Erreur lors de l'archivage")
            .build();
    } catch (Exception e) {
        // ✅ Log l'erreur pour debugging
        System.err.println("Erreur inattendue archivage: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Error-Message", "Erreur serveur")
            .build();
    }
}

/**
 * Désarchive un dossier
 * ✅ CORRECTION : URL corrigée pour correspondre au service Angular
 */
@PostMapping("/{id}/desarchiver")
public ResponseEntity<DossierRecouvrement> desarchiverDossier(@PathVariable Long id) {
    try {
        DossierRecouvrement dossier = dossierService.desarchiverDossier(id);
        return ResponseEntity.ok(dossier);
    } catch (IllegalStateException e) {
        return ResponseEntity.badRequest()
            .header("Error-Message", e.getMessage())
            .build();
    } catch (RuntimeException e) {
        if (e.getMessage().contains("non trouvé")) {
            return ResponseEntity.notFound().build();
        }
        System.err.println("Erreur désarchivage: " + e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Error-Message", "Erreur lors du désarchivage")
            .build();
    } catch (Exception e) {
        System.err.println("Erreur inattendue désarchivage: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .header("Error-Message", "Erreur serveur")
            .build();
    }
}
}