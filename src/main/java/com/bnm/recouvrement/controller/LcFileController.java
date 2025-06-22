package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.LcFile;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.LcFileService;
import com.bnm.recouvrement.service.DossierRecouvrementService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/lc")
@CrossOrigin(origins = "http://localhost:4200")
public class LcFileController {

    private static final Logger logger = LoggerFactory.getLogger(LcFileController.class);
    private static final String BASE_PATH = "./uploads/lc-files/";

    private final LcFileService lcFileService;
    private final DossierRecouvrementService dossierRecouvrementService;

    public LcFileController(LcFileService lcFileService, DossierRecouvrementService dossierRecouvrementService) {
        this.lcFileService = lcFileService;
        this.dossierRecouvrementService = dossierRecouvrementService;
    }

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<LcFile> uploadLcFile(
            @PathVariable Long dossierId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "lcNumber", required = false) String lcNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) String dateEcheance
    ) throws IOException {
        DossierRecouvrement dossier = dossierRecouvrementService.getDossierById(dossierId)
                .orElseThrow(() -> new RuntimeException("Dossier not found"));

        LcFile lcFile = new LcFile();
        lcFile.setTitle(title);
        lcFile.setLcNumber(lcNumber);
        lcFile.setMontant(montant);
        if (dateEcheance != null) {
            lcFile.setDateEcheance(java.time.LocalDateTime.parse(dateEcheance));
        }
        lcFile.setDossierRecouvrement(dossier);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LcFile savedLcFile = lcFileService.saveLcFile(lcFile, file, username);

        return ResponseEntity.ok(savedLcFile);
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<LcFile>> getLcFilesByDossierId(@PathVariable Long dossierId) {
        List<LcFile> lcFiles = lcFileService.getLcFilesByDossierId(dossierId);
        return ResponseEntity.ok(lcFiles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<LcFile> getLcFileById(@PathVariable Long id) {
        Optional<LcFile> lcFile = lcFileService.getLcFileById(id);
        return lcFile.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<LcFile> updateLcFile(
            @PathVariable Long id,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "lcNumber", required = false) String lcNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) String dateEcheance
    ) throws IOException {
        LcFile updatedLcFile = new LcFile();
        updatedLcFile.setTitle(title);
        updatedLcFile.setLcNumber(lcNumber);
        updatedLcFile.setMontant(montant);
        if (dateEcheance != null) {
            updatedLcFile.setDateEcheance(java.time.LocalDateTime.parse(dateEcheance));
        }

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        LcFile savedLcFile = lcFileService.updateLcFile(id, updatedLcFile, file, username);

        if (savedLcFile != null) {
            return ResponseEntity.ok(savedLcFile);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteLcFile(@PathVariable Long id) {
        boolean deleted = lcFileService.deleteLcFile(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadLcFile(
            @PathVariable Long id,
            @RequestParam(value = "download", defaultValue = "false") boolean download) throws IOException {
        byte[] fileContent = lcFileService.getLcFileContent(id);
        Optional<LcFile> lcFile = lcFileService.getLcFileById(id);
        if (lcFile.isPresent()) {
            // Extract filename from filepath
            String fileName = "lc_file";
            if (lcFile.get().getFilePath() != null) {
                String filePath = lcFile.get().getFilePath();
                int lastSlashIndex = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
                if (lastSlashIndex >= 0) {
                    fileName = filePath.substring(lastSlashIndex + 1);
                } else {
                    fileName = filePath;
                }
            }

            // Determine content type based on file extension
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (fileName.toLowerCase().endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            } else if (fileName.toLowerCase().endsWith(".jpg") || fileName.toLowerCase().endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (fileName.toLowerCase().endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);

            // Set content disposition based on download parameter
            if (download) {
                headers.setContentDispositionFormData("attachment", fileName);
            } else {
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"");
            }

            return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> getLcFilePdf(@PathVariable Long id) {
        DossierRecouvrement dossier = dossierRecouvrementService.getDossierById(id)
                .orElseThrow(() -> new RuntimeException("Dossier non trouvé"));

        // Ici, dossier.getLcFile() doit être **uniquement** le nom du fichier
        // Récupérer le nom du fichier à partir de l'URL
        String fileName = dossier.getLcFiles() != null && !dossier.getLcFiles().isEmpty() ? 
                dossier.getLcFiles().get(0).getFilePath().substring(dossier.getLcFiles().get(0).getFilePath().lastIndexOf('/') + 1) : 
                null;
        String filePath = fileName != null ? BASE_PATH + fileName : null;

        try {
            Path path = Paths.get(filePath);
            if (filePath != null && Files.exists(path)) {
                byte[] pdfBytes = Files.readAllBytes(path);

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("inline", fileName);

                return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            } else {
                logger.warn("File not found: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (IOException e) {
            logger.error("Error reading LC file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}