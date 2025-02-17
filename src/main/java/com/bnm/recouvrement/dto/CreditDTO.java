package com.bnm.recouvrement.dto;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class CreditDTO {
    private Double montant;
    private Double tauxInteret;
    private Integer duree; // En mois
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")    private LocalDate dateDebut;
    private String statut;
    private String refTransaction;
    private String idCompte; // Pour mapper l'id du compte
    private Long idGarantie;
    private String fondDossier; // Pour mapper l'id de la garantie

    // Getters et Setters
    

    
    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Double getTauxInteret() {
        return tauxInteret;
    }

    public void setTauxInteret(Double tauxInteret) {
        this.tauxInteret = tauxInteret;
    }

    public Integer getDuree() {
        return duree;
    }

    public void setDuree(Integer duree) {
        this.duree = duree;
    }

    public LocalDate getDateDebut() {
        return dateDebut;
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getRefTransaction() {
        return refTransaction;
    }

    public void setRefTransaction(String refTransaction) {
        this.refTransaction = refTransaction;
    }

    public String getIdCompte() {
        return idCompte;
    }

    public void setIdCompte(String idCompte) {
        this.idCompte = idCompte;
    }




    public String getFondDossier() {
        return fondDossier;
    }
    
    public void setFondDossier(String fondDossier) {
        this.fondDossier = fondDossier;
    }

    public Long getIdGarantie() {
        return idGarantie;
    }

    public void setIdGarantie(Long idGarantie) {
        this.idGarantie = idGarantie;
    }

}
