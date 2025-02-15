package com.bnm.recouvrement.entity;


import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.NoArgsConstructor;
import java.util.Objects;

@NoArgsConstructor
@Entity
public class DossierRecouvrement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDossier;

    public Long getIdDossier() {
        return idDossier;
    }

    public void setIdDossier(Long idDossier) {
        this.idDossier = idDossier;
    }

    public Credit getCredit() {
        return credit;
    }

    public void setCredit(Credit credit) {
        this.credit = credit;
    }

    

    public int getImpaye() {
        return impaye;
    }

    public void setImpaye(int impaye) {
        this.impaye = impaye;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @OneToOne
    @JoinColumn(name = "id_credit", nullable = true)
    
    private Credit credit;

    

    @Column(nullable = false)
    private int impaye;

    private LocalDateTime dateCreation;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Long sold;

    @Column(nullable = false)
    private String PR;

    @Column(nullable = false)
    private String PRP;

    @Column(nullable = false)
    private float ajout;

    @Column(nullable = false)
    private String encour;

    @Column(nullable = false)
    private float principearemboursse;

    public DossierRecouvrement(Credit credit, String etat, int impaye) {
        this.credit = credit;
        
        this.impaye = impaye;
    }

    

    public DossierRecouvrement(Long idDossier, Credit credit, int impaye, LocalDateTime dateCreation, String accountNumber, String status, Long sold, String PR, String PRP, float ajout, String encour, float principearemboursse) {
        this.idDossier = idDossier;
        this.credit = credit;
        this.impaye = impaye;
        this.dateCreation = dateCreation;
        this.accountNumber = accountNumber;
        this.status = status;
        this.sold = sold;
        this.PR = PR;
        this.PRP = PRP;
        this.ajout = ajout;
        this.encour = encour;
        this.principearemboursse = principearemboursse;
    }

    public Long getSold() {
        return this.sold;
    }

    public void setSold(Long sold) {
        this.sold = sold;
    }

    public String getPR() {
        return this.PR;
    }

    public void setPR(String PR) {
        this.PR = PR;
    }

    public String getPRP() {
        return this.PRP;
    }

    public void setPRP(String PRP) {
        this.PRP = PRP;
    }

    public float getAjout() {
        return this.ajout;
    }

    public void setAjout(float ajout) {
        this.ajout = ajout;
    }

    public String getEncour() {
        return this.encour;
    }

    public void setEncour(String encour) {
        this.encour = encour;
    }

    public float getPrincipearemboursse() {
        return this.principearemboursse;
    }

    public void setPrincipearemboursse(float principearemboursse) {
        this.principearemboursse = principearemboursse;
    }

    public DossierRecouvrement idDossier(Long idDossier) {
        setIdDossier(idDossier);
        return this;
    }

    public DossierRecouvrement credit(Credit credit) {
        setCredit(credit);
        return this;
    }

    public DossierRecouvrement impaye(int impaye) {
        setImpaye(impaye);
        return this;
    }

    public DossierRecouvrement dateCreation(LocalDateTime dateCreation) {
        setDateCreation(dateCreation);
        return this;
    }

    public DossierRecouvrement accountNumber(String accountNumber) {
        setAccountNumber(accountNumber);
        return this;
    }

    public DossierRecouvrement status(String status) {
        setStatus(status);
        return this;
    }

    public DossierRecouvrement sold(Long sold) {
        setSold(sold);
        return this;
    }

    public DossierRecouvrement PR(String PR) {
        setPR(PR);
        return this;
    }

    public DossierRecouvrement PRP(String PRP) {
        setPRP(PRP);
        return this;
    }

    public DossierRecouvrement ajout(float ajout) {
        setAjout(ajout);
        return this;
    }

    public DossierRecouvrement encour(String encour) {
        setEncour(encour);
        return this;
    }

    public DossierRecouvrement principearemboursse(float principearemboursse) {
        setPrincipearemboursse(principearemboursse);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof DossierRecouvrement)) {
            return false;
        }
        DossierRecouvrement dossierRecouvrement = (DossierRecouvrement) o;
        return Objects.equals(idDossier, dossierRecouvrement.idDossier) && Objects.equals(credit, dossierRecouvrement.credit) && impaye == dossierRecouvrement.impaye && Objects.equals(dateCreation, dossierRecouvrement.dateCreation) && Objects.equals(accountNumber, dossierRecouvrement.accountNumber) && Objects.equals(status, dossierRecouvrement.status) && Objects.equals(sold, dossierRecouvrement.sold) && Objects.equals(PR, dossierRecouvrement.PR) && Objects.equals(PRP, dossierRecouvrement.PRP) && ajout == dossierRecouvrement.ajout && Objects.equals(encour, dossierRecouvrement.encour) && principearemboursse == dossierRecouvrement.principearemboursse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idDossier, credit, impaye, dateCreation, accountNumber, status, sold, PR, PRP, ajout, encour, principearemboursse);
    }

    @Override
    public String toString() {
        return "{" +
            " idDossier='" + getIdDossier() + "'" +
            ", credit='" + getCredit() + "'" +
            ", impaye='" + getImpaye() + "'" +
            ", dateCreation='" + getDateCreation() + "'" +
            ", accountNumber='" + getAccountNumber() + "'" +
            ", status='" + getStatus() + "'" +
            ", sold='" + getSold() + "'" +
            ", PR='" + getPR() + "'" +
            ", PRP='" + getPRP() + "'" +
            ", ajout='" + getAjout() + "'" +
            ", encour='" + getEncour() + "'" +
            ", principearemboursse='" + getPrincipearemboursse() + "'" +
            "}";
    }
    

}

