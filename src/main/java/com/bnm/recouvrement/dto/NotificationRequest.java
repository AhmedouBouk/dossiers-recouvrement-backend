package com.bnm.recouvrement.dto;

/**
 * DTO pour les requêtes de notification
 */
public class NotificationRequest {
    private String message;
    private String type;
    private Long destinataireId;
    private String destinataireRole;
    private String lienUrl;
    private String titre;
    private String contenu;

    // Constructeurs
    public NotificationRequest() {
    }

    public NotificationRequest(String message, String type, String lienUrl) {
        this.message = message;
        this.type = type;
        this.lienUrl = lienUrl;
    }
    
    public NotificationRequest(String message, String type, String lienUrl, String titre, String contenu) {
        this.message = message;
        this.type = type;
        this.lienUrl = lienUrl;
        this.titre = titre;
        this.contenu = contenu;
    }

    // Getters et Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getDestinataireId() {
        return destinataireId;
    }

    public void setDestinataireId(Long destinataireId) {
        this.destinataireId = destinataireId;
    }

    public String getDestinataireRole() {
        return destinataireRole;
    }

    public void setDestinataireRole(String destinataireRole) {
        this.destinataireRole = destinataireRole;
    }

    public String getLienUrl() {
        return lienUrl;
    }

    public void setLienUrl(String lienUrl) {
        this.lienUrl = lienUrl;
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
}
