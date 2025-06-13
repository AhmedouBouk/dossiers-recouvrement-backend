package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.Garantie;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.GarantieService;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/garantie")
@CrossOrigin(origins = "http://localhost:4200")
public class GarantieController {

    @Autowired
    private GarantieService garantieService;
    @Autowired
    private DossierRecouvrementRepository dossierRecouvrementRepository;

    private static final Logger logger = LoggerFactory.getLogger(GarantieController.class);

    // Uploader un fichier de garantie
    @PostMapping("/{dossierId}/garantie")
    public ResponseEntity<DossierRecouvrement> uploadGarantie(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("titre") String titre) throws IOException {
        DossierRecouvrement dossier = garantieService.uploadGarantie(dossierId, file, titre);
        return ResponseEntity.ok(dossier);
    }
    
    // Récupérer toutes les garanties d'un dossier
    @GetMapping("/{dossierId}/garanties")
    public ResponseEntity<List<Garantie>> getGarantiesByDossierId(@PathVariable Long dossierId) {
        List<Garantie> garanties = garantieService.getGarantiesByDossierId(dossierId);
        return ResponseEntity.ok(garanties);
    }

    // Récupérer une garantie par son ID
    @GetMapping("/garantie/{garantieId}")
    public ResponseEntity<Garantie> getGarantieById(@PathVariable Long garantieId) {
        try {
            Garantie garantie = garantieService.getGarantieById(garantieId);
            return ResponseEntity.ok(garantie);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/garantie/{garantieId}")
    public ResponseEntity<Garantie> updateGarantie(
            @PathVariable Long garantieId,
            @RequestParam("titre") String nouveauTitre) {
        Garantie updatedGarantie = garantieService.updateGarantie(garantieId, nouveauTitre);
        return ResponseEntity.ok(updatedGarantie);
    }
    // Supprimer un fichier de garantie
    @DeleteMapping("/garantie/{garantieId}")
    public ResponseEntity<Map<String, String>> deleteGarantie(@PathVariable Long garantieId) {
        try {
            garantieService.deleteGarantie(garantieId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Garantie supprimée avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la suppression de la garantie");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @DeleteMapping("/{dossierId}/garanties/delete-all")
    public ResponseEntity<Map<String, String>> deleteAllGaranties(@PathVariable Long dossierId) {
        try {
            garantieService.deleteAllGarantiesByDossierId(dossierId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Toutes les garanties ont été supprimées avec succès");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Erreur lors de la suppression des garanties");
            return ResponseEntity.status(500).body(response);
        }
    }

    @GetMapping("/garantie/{garantieId}/pdf")
    public ResponseEntity<Resource> getGarantiePdf(@PathVariable Long garantieId, @RequestParam(value = "download", defaultValue = "false") boolean download) {
        try {
            Garantie garantie = garantieService.getGarantieById(garantieId);
            if (garantie == null || garantie.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }
            
            // Extract the filename from the stored path
            String filePath = garantie.getFilePath();
            String fileName;
            if (filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            } else {
                fileName = filePath;
            }
            
            // Resolve the actual file path
            Path fileLocation = Paths.get("uploads/garanties").resolve(fileName).normalize().toAbsolutePath();
            Resource resource = new UrlResource(fileLocation.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                // Determine content type based on file extension
                String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
                if (fileName.toLowerCase().endsWith(".pdf")) {
                    contentType = MediaType.APPLICATION_PDF_VALUE;
                } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                    contentType = MediaType.IMAGE_JPEG_VALUE;
                } else if (fileName.toLowerCase().endsWith(".png")) {
                    contentType = MediaType.IMAGE_PNG_VALUE;
                } else if (fileName.toLowerCase().endsWith(".gif")) {
                    contentType = MediaType.IMAGE_GIF_VALUE;
                }
                
                // Set content disposition based on download parameter
                String contentDisposition;
                if (download) {
                    contentDisposition = "attachment; filename=\"" + fileName + "\"";
                } else {
                    contentDisposition = "inline; filename=\"" + fileName + "\"";
                }
                
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition)
                        .body(resource);
            } else {
                logger.error("File not found or not readable: {}", fileLocation.toString());
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            logger.error("Malformed URL for garantie file path: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            logger.error("Error reading garantie file for ID {}: {}", garantieId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

}