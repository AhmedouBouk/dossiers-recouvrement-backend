package com.bnm.recouvrement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "garanties")
public class Garantie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(nullable = false)
    private String filePath;

    @Column(name = "upload_date", nullable = false)
    private LocalDateTime uploadDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dossier_id", nullable = false)
    @JsonBackReference
    private DossierRecouvrement dossier;

    // Constructors
    public Garantie() {}

    public Garantie(String titre, String filePath, LocalDateTime uploadDate, DossierRecouvrement dossier) {
        this.titre = titre;
        this.filePath = filePath;
        this.uploadDate = uploadDate;
        this.dossier = dossier;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
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