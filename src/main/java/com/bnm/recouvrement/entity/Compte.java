package com.bnm.recouvrement.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Compte {

    // Define the relationship with Client
   @ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "nni", referencedColumnName = "NNI")
@JsonBackReference
private Client client;


    // Primary key
    @Id
    @Column(name = "nomCompte", nullable = false)
    private String nomCompte;

    @Column(name = "libelecategorie", nullable = false)
    private String libelecategorie;
    @Column(name = "categorie", nullable = false)
    private int categorie;

    @Column(name = "solde")
    private Double solde;

    @Column(name = "Etat", length = 50)
    private String etat;

    @Column(name = "DateOuverture")
    private LocalDate dateOuverture;

    // Constructor
    public Compte(Client client, String nomCompte, String libelecategorie, Double solde, String etat, LocalDate dateOuverture,int catagorie) {
        this.client = client;
        this.nomCompte = nomCompte;
        this.libelecategorie = libelecategorie;
        this.solde = solde;
        this.etat = etat;
        this.dateOuverture = dateOuverture;
        this.categorie=catagorie;
    }

    // Getters and Setters
    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

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
}
