package com.bnm.recouvrement.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "dossier_id")
    private DossierRecouvrement dossier;
    
    @Column(nullable = false)
    private String titre;
    
    @Column(nullable = false, length = 1000)
    private String contenu;
    
    @Column(nullable = false)
    private LocalDateTime dateCreation = LocalDateTime.now();
    
    @Column(nullable = false)
    private boolean lu = false;
    
    @Column(nullable = false)
    private String type; // 'GARANTIE', 'CHEQUE', 'DOCUMENT_DO', etc.
    
    @Column(name = "message", length = 1000)
    private String message;
    
    @Column(name = "lien_url")
    private String lienUrl;

    public Notification() {}
    
    public Notification(User user, DossierRecouvrement dossier, String titre, String contenu, String type) {
        this.user = user;
        this.dossier = dossier;
        this.titre = titre;
        this.contenu = contenu;
        this.type = type;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DossierRecouvrement getDossier() {
        return dossier;
    }

    public void setDossier(DossierRecouvrement dossier) {
        this.dossier = dossier;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public boolean isLu() {
        return lu;
    }

    public void setLu(boolean lu) {
        this.lu = lu;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getLienUrl() {
        return lienUrl;
    }
    
    public void setLienUrl(String lienUrl) {
        this.lienUrl = lienUrl;
    }
}