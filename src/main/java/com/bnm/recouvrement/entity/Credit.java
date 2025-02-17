package com.bnm.recouvrement.entity;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class Credit {

    @Id
    private Long idCredit;

    public Long getIdCredit() {
        return idCredit;
    }


    public void setIdCredit(Long idCredit) {
        this.idCredit = idCredit;
    }

    @ManyToOne
    @JoinColumn(name = "id_compte", nullable = false)
    private Compte compte;
     @OneToOne(mappedBy = "credit", fetch = FetchType.LAZY)
    @JsonIgnore // Ignorer cette propriété lors de la sérialisation JSON
    private DossierRecouvrement dossierRecouvrement;

    public DossierRecouvrement getDossierRecouvrement() {
        return dossierRecouvrement;
    }


    public void setDossierRecouvrement(DossierRecouvrement dossierRecouvrement) {
        this.dossierRecouvrement = dossierRecouvrement;
    }

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_garantie", nullable = false)
    private Garantie garantie;
    
    public Garantie getGarantie() {
        return garantie;
    }


    public void setGarantie(Garantie garantie) {
        this.garantie = garantie;
    }

    @Column( name = "montant")
    private Double montant;
    @Column( name = "tauxInteret")
    private Double tauxInteret;
    @Column( name = "duree")
    private Integer duree; // En mois
    @Column( name = "dateDebut")
    private LocalDate dateDebut;
    @Column( name = "statut")
    private String statut;
    
    private String refTransaction;


    @Column( name = "fondDossier")
    private String fondDossier;
    
   



    // Constructeur
    public Credit(Compte compte, Garantie garantie, Double montant, Double tauxInteret, Integer duree, LocalDate dateDebut, String statut) {
        this.compte = compte;
        this.montant = montant;
        this.tauxInteret = tauxInteret;
        this.duree = duree;
        this.dateDebut = dateDebut;
        this.statut = statut;
    }


    // Getters et setters

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

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getRefTransaction() {
        return refTransaction;
    }

    public void setRefTransaction(String refTransaction) {
        this.refTransaction = refTransaction;
    }

    public String getFondDossier() {
        return fondDossier;
    }
    
    public void setFondDossier(String fondDossier) {
        this.fondDossier = fondDossier;
    }
 
}

