package com.bnm.recouvrement.controller;

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
            @RequestParam("title") String title,
            @RequestParam(value = "cautionNumber", required = false) String cautionNumber,
            @RequestParam(value = "montant", required = false) Double montant,
            @RequestParam(value = "dateEcheance", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateEcheance) {
        CautionFile updatedCautionFile = cautionFileService.updateCautionFile(id, title, cautionNumber, montant, dateEcheance);
        return ResponseEntity.ok(updatedCautionFile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCautionFile(@PathVariable Long id) throws IOException {
        cautionFileService.deleteCautionFile(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<ByteArrayResource> downloadCautionFile(@PathVariable Long id) throws IOException {
        CautionFile cautionFile = cautionFileService.getCautionFileById(id)
                .orElseThrow(() -> new RuntimeException("CautionFile not found with id: " + id));

        byte[] fileContent = cautionFileService.getCautionFileContent(id);
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"caution_file_" + cautionFile.getTitle() + "\"");
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(fileContent.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }
}
