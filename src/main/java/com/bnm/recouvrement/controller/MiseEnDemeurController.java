package com.bnm.recouvrement.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.service.MiseEnDemeurService;
import com.itextpdf.text.DocumentException;

@RestController
@RequestMapping("/api/mise-en-demeure")
public class MiseEnDemeurController{

    @Autowired
    private MiseEnDemeurService miseEnDemeureService;
    
    @GetMapping("/generer/{dossierId}")
    public ResponseEntity<byte[]> genererMiseEnDemeure(@PathVariable Long dossierId) {
        try {
            byte[] pdfContent = miseEnDemeureService.genererPdfMiseEnDemeure(dossierId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "mise-en-demeure-" + dossierId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (DocumentException e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}