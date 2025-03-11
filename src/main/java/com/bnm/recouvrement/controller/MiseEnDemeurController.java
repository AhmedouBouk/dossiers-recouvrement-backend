package com.bnm.recouvrement.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class MiseEnDemeurController {

    private static final Logger logger = LoggerFactory.getLogger(MiseEnDemeurController.class);

    @Autowired
    private MiseEnDemeurService miseEnDemeureService;

    @GetMapping("/generer/{dossierId}")
    public ResponseEntity<byte[]> genererMiseEnDemeure(@PathVariable Long dossierId) {
        logger.info("Tentative de génération de mise en demeure pour le dossier: {}", dossierId);
        try {
            // Vérifier si le dossier existe
            logger.debug("Vérification de l'existence du dossier {}", dossierId);
            
            // Générer le PDF
            logger.debug("Génération du PDF pour le dossier {}", dossierId);
            byte[] pdfContent = miseEnDemeureService.genererPdfMiseEnDemeure(dossierId);
            logger.debug("PDF généré avec succès, taille: {} octets", pdfContent.length);

            // Configuration des en-têtes
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "mise-en-demeure-" + dossierId + ".pdf");
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            logger.info("Mise en demeure générée avec succès pour le dossier: {}", dossierId);
            return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
        } catch (DocumentException e) {
            logger.error("Erreur DocumentException lors de la génération du PDF pour le dossier {}: {}", dossierId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            // Capturer toutes les autres exceptions pour avoir plus d'informations
            logger.error("Erreur inattendue lors de la génération du PDF pour le dossier {}: {}", dossierId, e.getMessage(), e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}