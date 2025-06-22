package com.bnm.recouvrement.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bnm.recouvrement.entity.CreditFile;
import com.bnm.recouvrement.service.CreditsService;

@RestController
@RequestMapping("/credits")
@CrossOrigin(origins = "http://localhost:4200")
public class CreditsController {

    private final CreditsService creditsService;
    
    public CreditsController(CreditsService creditsService) {
        this.creditsService = creditsService;
    }

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<CreditFile> uploadCreditFile(
            @PathVariable Long dossierId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "creditNumber", required = false) String creditNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEcheance) throws IOException {
        CreditFile savedCreditFile = creditsService.saveCreditFile(dossierId, title, file, creditNumber, montant, dateEcheance);
        return ResponseEntity.ok(savedCreditFile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CreditFile> getCreditFileById(@PathVariable Long id) {
        return creditsService.getCreditFileById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<CreditFile>> getCreditFilesByDossierId(@PathVariable Long dossierId) {
        List<CreditFile> creditFiles = creditsService.getCreditFilesByDossierId(dossierId);
        return ResponseEntity.ok(creditFiles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CreditFile> updateCreditFile(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam(value = "creditNumber", required = false) String creditNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEcheance) {
        CreditFile updatedCreditFile = creditsService.updateCreditFile(id, title, creditNumber, montant, dateEcheance);
        return ResponseEntity.ok(updatedCreditFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCreditFile(@PathVariable Long id) throws IOException {
        creditsService.deleteCreditFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadCreditFile(
            @PathVariable Long id, 
            @RequestParam(value = "download", defaultValue = "false") boolean download) throws IOException {
        CreditFile creditFile = creditsService.getCreditFileById(id)
                .orElseThrow(() -> new RuntimeException("CreditFile not found with id: " + id));

        byte[] fileContent = creditsService.getCreditFileContent(id);
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        HttpHeaders headers = new HttpHeaders();
        
        // Get filename from filepath
        String filename = "credit_file";
        if (creditFile.getFilePath() != null) {
            String[] pathParts = creditFile.getFilePath().split("/");
            filename = pathParts[pathParts.length - 1];
        }
        
        // Determine content type based on file extension
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (filename.toLowerCase().endsWith(".pdf")) {
            mediaType = MediaType.APPLICATION_PDF;
        } else if (filename.toLowerCase().endsWith(".jpg") || filename.toLowerCase().endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (filename.toLowerCase().endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        }
        
        // Set content disposition based on download parameter
        String contentDisposition = download ? 
            "attachment; filename=\"" + filename + "\"" : 
            "inline; filename=\"" + filename + "\"";
        
        headers.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition);
        headers.setContentType(mediaType);
        headers.setContentLength(fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}