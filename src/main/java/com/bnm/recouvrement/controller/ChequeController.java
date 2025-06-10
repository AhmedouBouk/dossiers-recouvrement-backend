package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.dto.ChequeFileUpdateDto;
import com.bnm.recouvrement.entity.ChequeFile;
import com.bnm.recouvrement.service.ChequeService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.format.annotation.DateTimeFormat;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/cheques") // Consistent API prefix
@CrossOrigin(origins = "http://localhost:4200")
public class ChequeController {

    private static final Logger logger = LoggerFactory.getLogger(ChequeController.class);
    private final ChequeService chequeService;
    private static final Path FILE_STORAGE_LOCATION = Paths.get("uploads/cheques").toAbsolutePath().normalize();

    public ChequeController(ChequeService chequeService) {
        this.chequeService = chequeService;
    }

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<ChequeFile> uploadChequeFile(@PathVariable Long dossierId,
                                                       @RequestParam("title") String title,
                                                       @RequestParam(value = "chequeNumber", required = false) String chequeNumber,
                                                       @RequestParam(value = "montant", required = false) Double montant,
                                                       @RequestParam(value = "dateEcheance", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEcheance,
                                                       @RequestParam("file") MultipartFile file) {
        try {
            ChequeFile chequeFile = chequeService.uploadChequeFile(dossierId, title, chequeNumber, montant, dateEcheance, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(chequeFile);
        } catch (IOException e) {
            logger.error("Could not upload cheque file for dossier ID {}: {}", dossierId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            logger.error("Error during cheque file upload for dossier ID {}: {}", dossierId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Or more specific error
        }
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<ChequeFile>> getChequeFilesByDossierId(@PathVariable Long dossierId) {
        List<ChequeFile> chequeFiles = chequeService.getChequeFilesByDossierId(dossierId);
        return ResponseEntity.ok(chequeFiles);
    }

    @GetMapping("/{chequeFileId}")
    public ResponseEntity<ChequeFile> getChequeFileById(@PathVariable Long chequeFileId) {
        return chequeService.getChequeFileById(chequeFileId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{chequeFileId}")
    public ResponseEntity<ChequeFile> updateChequeFile(@PathVariable Long chequeFileId,
                                                     @RequestBody ChequeFileUpdateDto updateDto) {
        try {
            ChequeFile updatedChequeFile = chequeService.updateChequeFile(chequeFileId, 
                                                                        updateDto.getTitle(), 
                                                                        updateDto.getChequeNumber(), 
                                                                        updateDto.getMontant(), 
                                                                        updateDto.getDateEcheance());
            return ResponseEntity.ok(updatedChequeFile);
        } catch (RuntimeException e) {
            logger.error("Could not update cheque file with ID {}: {}", chequeFileId, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{chequeFileId}")
    public ResponseEntity<Void> deleteChequeFile(@PathVariable Long chequeFileId) {
        try {
            chequeService.deleteChequeFile(chequeFileId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            logger.error("IO error deleting cheque file ID {}: {}", chequeFileId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (RuntimeException e) {
            logger.error("Error deleting cheque file ID {}: {}", chequeFileId, e.getMessage());
            return ResponseEntity.notFound().build(); // e.g., file not found
        }
    }

    @DeleteMapping("/dossier/{dossierId}/all")
    public ResponseEntity<Void> deleteAllChequeFilesByDossierId(@PathVariable Long dossierId) {
        try {
            chequeService.deleteAllChequeFilesByDossierId(dossierId);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            logger.error("IO error deleting all cheque files for dossier ID {}: {}", dossierId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/download/{chequeFileId}")
    public ResponseEntity<Resource> downloadChequeFile(
            @PathVariable Long chequeFileId,
            @RequestParam(value = "download", defaultValue = "false") boolean download) {
        ChequeFile chequeFile = chequeService.getChequeFileById(chequeFileId)
                .orElseThrow(() -> new RuntimeException("ChequeFile not found with id: " + chequeFileId));

        try {
            // Extract the filename from the stored path
            String filePath = chequeFile.getFilePath();
            String fileName;
            if (filePath != null && filePath.contains("/")) {
                fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            } else {
                fileName = filePath;
            }
            
            // Resolve the actual file path
            Path fileLocation = Paths.get("uploads/cheques").resolve(fileName).normalize().toAbsolutePath();
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
            logger.error("Malformed URL for file path {}: {}", chequeFile.getFilePath(), e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}