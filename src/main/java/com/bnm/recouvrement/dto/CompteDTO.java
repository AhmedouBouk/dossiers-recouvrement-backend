package com.bnm.recouvrement.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompteDTO {
    private String nomCompte;
    private String libelecategorie;
    private int categorie;
    private Double solde;
    private String etat;
    private LocalDate dateOuverture;
    private ClientDTO client;
    
    // Getters and Setters
    public String getNomCompte() {
        return nomCompte;
    }
    
    public void setNomCompte(String nomCompte) {
        this.nomCompte = nomCompte;
    }
    
    public String getLibelecategorie() {
        return libelecategorie;
    }
    
    public void setLibelecategorie(String libelecategorie) {
        this.libelecategorie = libelecategorie;
    }
    
    public int getCategorie() {
        return categorie;
    }
    
    public void setCategorie(int categorie) {
        this.categorie = categorie;
    }
    
    public Double getSolde() {
        return solde;
    }
    
    public void setSolde(Double solde) {
        this.solde = solde;
    }
    
    public String getEtat() {
        return etat;
    }
    
    public void setEtat(String etat) {
        this.etat = etat;
    }
    
    public LocalDate getDateOuverture() {
        return dateOuverture;
    }
    
    public void setDateOuverture(LocalDate dateOuverture) {
        this.dateOuverture = dateOuverture;
    }
    
    public ClientDTO getClient() {
        return client;
    }
    
    public void setClient(ClientDTO client) {
        this.client = client;
    }
}