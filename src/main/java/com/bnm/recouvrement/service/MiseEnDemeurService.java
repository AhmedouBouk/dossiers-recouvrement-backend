package com.bnm.recouvrement.service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bnm.recouvrement.entity.Client;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.MiseEndemeur;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class MiseEnDemeurService {

    private static final Logger logger = LoggerFactory.getLogger(MiseEnDemeurService.class);

    @Autowired
    private DossierRecouvrementService dossierService;

    /**
     * Crée une mise en demeure pour un dossier de recouvrement donné.
     *
     * @param dossierId L'ID du dossier de recouvrement.
     * @return Une instance de MiseEndemeur.
     * @throws IllegalArgumentException Si le dossier, le compte ou le client n'est pas trouvé.
     */
    public MiseEndemeur creerMiseEnDemeure(Long dossierId) {
        logger.info("Création d'une mise en demeure pour le dossier: {}", dossierId);

        // Récupérer les informations du dossier
        Optional<DossierRecouvrement> dossierOpt = dossierService.getDossierById(dossierId);

        if (!dossierOpt.isPresent()) {
            logger.error("Dossier non trouvé avec l'ID: {}", dossierId);
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + dossierId);
        }

        DossierRecouvrement dossier = dossierOpt.get();
        Compte compte = dossier.getCompte();

        if (compte == null) {
            logger.error("Compte non trouvé pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Compte non trouvé pour le dossier: " + dossierId);
        }

        Client client = compte.getClient();

        if (client == null) {
            logger.error("Client non trouvé pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Client non trouvé pour le dossier: " + dossierId);
        }

        // Vérifier les données obligatoires
        if (client.getNom() == null || client.getNom().trim().isEmpty()) {
            logger.error("Nom du client manquant pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Nom du client manquant");
        }

        if (client.getPrenom() == null || client.getPrenom().trim().isEmpty()) {
            logger.error("Prénom du client manquant pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Prénom du client manquant");
        }

        if (client.getAdresse() == null || client.getAdresse().trim().isEmpty()) {
            logger.error("Adresse du client manquante pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Adresse du client manquante");
        }

        if (compte.getNomCompte() == null || compte.getNomCompte().trim().isEmpty()) {
            logger.error("Numéro de compte manquant pour le dossier: {}", dossierId);
            throw new IllegalArgumentException("Numéro de compte manquant");
        }

        // Créer la mise en demeure
        try {
            MiseEndemeur miseEnDemeure = new MiseEndemeur();
            miseEnDemeure.setDossierId(dossierId);
            miseEnDemeure.setReference("MD-" + dossierId + "-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            miseEnDemeure.setDateCreation(LocalDate.now());
            miseEnDemeure.setNomClient(client.getNom());
            miseEnDemeure.setPrenomClient(client.getPrenom());
            miseEnDemeure.setAdresseClient(client.getAdresse());
            miseEnDemeure.setNumeroCompte(compte.getNomCompte());
            miseEnDemeure.setMontantDu(dossier.getEngagementTotal());
            miseEnDemeure.setStatus("GENEREE");

            // Générer le contenu de la mise en demeure
            String contenu = genererContenuMiseEnDemeure(dossier, client);
            miseEnDemeure.setContenu(contenu);

            logger.info("Mise en demeure créée avec succès pour le dossier: {}", dossierId);
            return miseEnDemeure;
        } catch (Exception e) {
            logger.error("Erreur lors de la création de la mise en demeure pour le dossier {}: {}", dossierId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Génère un fichier PDF pour la mise en demeure.
     *
     * @param dossierId L'ID du dossier de recouvrement.
     * @return Un tableau de bytes représentant le fichier PDF.
     * @throws DocumentException Si une erreur survient lors de la génération du PDF.
     */
    public byte[] genererPdfMiseEnDemeure(Long dossierId) throws DocumentException {
        logger.info("Génération du PDF de mise en demeure pour le dossier: {}", dossierId);
        try {
            MiseEndemeur miseEnDemeure = creerMiseEnDemeure(dossierId);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document();
            PdfWriter.getInstance(document, baos);

            document.open();

            // En-tête
            Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Font fontGras = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);

            // Logo et informations banque
            document.add(new Paragraph("BANQUE NATIONALE DE MAURITANIE", fontTitre));
            document.add(new Paragraph("Service de Recouvrement", fontGras));
            document.add(new Paragraph("Date : " + miseEnDemeure.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), fontNormal));
            document.add(new Paragraph("Référence : " + miseEnDemeure.getReference(), fontNormal));
            document.add(new Paragraph("\n"));

            // Informations client
            document.add(new Paragraph("À l'attention de :", fontGras));
            document.add(new Paragraph(miseEnDemeure.getPrenomClient() + " " + miseEnDemeure.getNomClient(), fontNormal));
            document.add(new Paragraph(miseEnDemeure.getAdresseClient(), fontNormal));
            document.add(new Paragraph("\n\n"));

            // Objet
            document.add(new Paragraph("Objet : MISE EN DEMEURE - COMPTE N° " + miseEnDemeure.getNumeroCompte(), fontGras));
            document.add(new Paragraph("\n"));

            // Corps du document
            document.add(new Paragraph("Madame, Monsieur,", fontNormal));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Nous constatons avec regret que malgré nos relances précédentes, votre compte n° "
                    + miseEnDemeure.getNumeroCompte() + " présente toujours un solde débiteur de "
                    + String.format("%,.2f", miseEnDemeure.getMontantDu()) + " MRU.", fontNormal));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Par la présente, nous vous mettons en demeure de régulariser cette situation dans un délai de 15 jours à compter de la réception de ce courrier.", fontNormal));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("À défaut de paiement dans le délai imparti, nous nous verrons dans l'obligation d'engager toutes les procédures légales de recouvrement à votre encontre, ce qui pourrait entraîner des frais supplémentaires à votre charge.", fontNormal));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Nous vous prions de prendre contact avec notre service de recouvrement dans les plus brefs délais pour convenir des modalités de règlement de cette dette.", fontNormal));
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Veuillez agréer, Madame, Monsieur, l'expression de nos salutations distinguées.", fontNormal));
            document.add(new Paragraph("\n\n"));
            document.add(new Paragraph("Le Service de Recouvrement", fontGras));
            document.add(new Paragraph("Banque Nationale de Mauritanie", fontNormal));

            document.close();

            logger.info("PDF généré avec succès pour le dossier: {}", dossierId);
            return baos.toByteArray();
        } catch (Exception e) {
            logger.error("Erreur lors de la génération du PDF pour le dossier {}: {}", dossierId, e.getMessage(), e);
            throw new DocumentException(e.getMessage(), e);
        }
    }

    /**
     * Génère le contenu textuel de la mise en demeure.
     *
     * @param dossier Le dossier de recouvrement.
     * @param client  Le client associé au dossier.
     * @return Le contenu textuel de la mise en demeure.
     */
    private String genererContenuMiseEnDemeure(DossierRecouvrement dossier, Client client) {
        StringBuilder sb = new StringBuilder();
        sb.append("MISE EN DEMEURE\n\n");
        sb.append("Client: ").append(client.getPrenom()).append(" ").append(client.getNom()).append("\n");
        sb.append("Compte: ").append(dossier.getCompte().getNomCompte()).append("\n");
        sb.append("Montant dû: ").append(String.format("%,.2f", dossier.getEngagementTotal())).append(" MRU\n\n");
        sb.append("Nous vous mettons en demeure de régler la somme due dans un délai de 15 jours.\n");

        return sb.toString();
    }
}