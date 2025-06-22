package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.dto.CautionFileUpdateDto;
import com.bnm.recouvrement.entity.CautionFile;
import com.bnm.recouvrement.service.CautionFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/caution-files")
public class CautionFileController {

    @Autowired
    private CautionFileService cautionFileService;

    @PostMapping("/upload/{dossierId}")
    public ResponseEntity<CautionFile> uploadCautionFile(
            @PathVariable Long dossierId,
            @RequestParam("title") String title,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cautionNumber", required = false) String cautionNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEcheance) throws IOException {
        CautionFile savedCautionFile = cautionFileService.saveCautionFile(dossierId, title, file, cautionNumber, montant, dateEcheance);
        return ResponseEntity.ok(savedCautionFile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CautionFile> getCautionFileById(@PathVariable Long id) {
        return cautionFileService.getCautionFileById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/dossier/{dossierId}")
    public ResponseEntity<List<CautionFile>> getCautionFilesByDossierId(@PathVariable Long dossierId) {
        List<CautionFile> cautionFiles = cautionFileService.getCautionFilesByDossierId(dossierId);
        return ResponseEntity.ok(cautionFiles);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CautionFile> updateCautionFile(
            @PathVariable Long id,
            @RequestBody CautionFileUpdateDto updateDto) {
        CautionFile updatedCautionFile = cautionFileService.updateCautionFile(
            id, 
            updateDto.getTitle(), 
            updateDto.getCautionNumber(), 
            updateDto.getMontant(), 
            updateDto.getDateEcheance());
        return ResponseEntity.ok(updatedCautionFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCautionFile(@PathVariable Long id) throws IOException {
        cautionFileService.deleteCautionFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadCautionFile(
        @PathVariable Long id,
        @RequestParam(value = "download", defaultValue = "false") boolean download) throws IOException {
    CautionFile cautionFile = cautionFileService.getCautionFileById(id)
            .orElseThrow(() -> new RuntimeException("CautionFile not found with id: " + id));

    byte[] fileContent = cautionFileService.getCautionFileContent(id);
    ByteArrayResource resource = new ByteArrayResource(fileContent);

    HttpHeaders headers = new HttpHeaders();
    
    // Get filename from filepath
    String filename = "caution_file";
    if (cautionFile.getFilePath() != null) {
        String[] pathParts = cautionFile.getFilePath().split("/");
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
