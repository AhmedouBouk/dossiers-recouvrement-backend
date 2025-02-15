package com.bnm.recouvrement.entity;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class Client {

    @Id
    @Column( name = "NNI")
    private Integer nni; // Numéro National d'Identification
    @Column( name = "NIF")
    private String nif; // Numéro d'Identification Fiscale
    @Column( name = "nom")
    private String nom; // Nom du client
    @Column( name = "Prenom")
    private String prenom; // Nom du client
    @Column( name = "DateNaissance")
    private LocalDate dateNaissance; // Date de naissance
    @Column( name = "SecteurActivation")
    private String secteurActivite; // Secteur d’activité
    @Column( name = "Genre")
    private String genre; // Genre (Masculin, Féminin)
    @Column( name = "Salaire")
    private Double salaire; // Salaire du client
    @Column( name = "Adresse")
    private String adresse;
  
@OneToMany(mappedBy = "client", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
private List<Compte> comptes;


     // Constructeur
     public Client(Integer nni, String nif, String nom, String prenom, LocalDate dateNaissance, 
     String secteurActivite, String genre, Double salaire, String adresse) {
        this.nni = nni;
        this.nif = nif;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.secteurActivite = secteurActivite;
        this.genre = genre;
        this.salaire = salaire;
        this.adresse = adresse;
    }

    //Getters and Setters

    public Integer getNni() {
        return nni;
    }

    public void setNni(Integer nni) {
        this.nni = nni;
    }

    public String getNif() {
        return nif;
    }

    public void setNif(String nif) {
        this.nif = nif;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getSecteurActivite() {
        return secteurActivite;
    }

    public void setSecteurActivite(String secteurActivite) {
        this.secteurActivite = secteurActivite;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Double getSalaire() {
        return salaire;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

}