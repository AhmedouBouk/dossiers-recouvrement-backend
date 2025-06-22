package com.bnm.recouvrement.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Table(name = "caution_files")
public class CautionFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private String filePath;

    private String cautionNumber;

    private Double montant;

    private LocalDateTime dateEcheance;

    private LocalDateTime uploadDate;

    @ManyToOne
    @JoinColumn(name = "dossier_id")
    @JsonBackReference
    private DossierRecouvrement dossier;

    // Constructors, getters, and setters
    public CautionFile() {
    }

    public CautionFile(String title, String filePath, String cautionNumber, Double montant, LocalDateTime dateEcheance, LocalDateTime uploadDate, DossierRecouvrement dossier) {
        this.title = title;
        this.filePath = filePath;
        this.cautionNumber = cautionNumber;
        this.montant = montant;
        this.dateEcheance = dateEcheance;
        this.uploadDate = uploadDate;
        this.dossier = dossier;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getCautionNumber() {
        return cautionNumber;
    }

    public void setCautionNumber(String cautionNumber) {
        this.cautionNumber = cautionNumber;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDateTime dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public LocalDateTime getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(LocalDateTime uploadDate) {
        this.uploadDate = uploadDate;
    }

    public DossierRecouvrement getDossier() {
        return dossier;
    }

    public void setDossier(DossierRecouvrement dossier) {
        this.dossier = dossier;
    }
}
