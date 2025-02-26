package com.bnm.recouvrement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.NoArgsConstructor;
@NoArgsConstructor

@Entity
public class Garantie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idGarantie;

    
    public Long getIdGarantie() {
        return idGarantie;
    }


    public void setIdGarantie(Long idGarantie) {
        this.idGarantie = idGarantie;
    }

    @Column( name = "typeGarantie")
    private String typeGarantie;
    @Column( name = "valeur")
    private Double valeur;

    @Column( name = "desciption")
    private String description;
    @Column( name = "fondDossier")
    private String fondDossier;


    // Constructeur
    public Garantie( String typeGarantie, Double valeur, String description) {
        this.typeGarantie = typeGarantie;
        this.valeur = valeur;
        this.description = description;
    }


    // Getters et setters

    public String getTypeGarantie() {
        return typeGarantie;
    }

    public void setTypeGarantie(String typeGarantie) {
        this.typeGarantie = typeGarantie;
    }

    public Double getValeur() {
        return valeur;
    }

    public void setValeur(Double valeur) {
        this.valeur = valeur;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFondDossier() {
        return fondDossier;
    }

    public void setFondDossier(String fondDossier) {
        this.fondDossier = fondDossier;
    }

   
    
}
