package com.bnm.recouvrement.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DossierRecouvrement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double engagementTotal;
    private Double montantPrincipal;
    private Double interetContractuel;
    private Double interetRetard;

 
    @Column(name = "nature")
    private String naturesEngagement; // credit, debit, cautions

    private String agenceOuvertureCompte;
    private String referencesChecks; // Format: TT/CodeAgence/NumeroCheck
    private String referencesCredits; // MG
    private String referencesCautions;
    private String referencesLC; // Lettres de Crédit
    private Double provision;
    private Double interetsReserves;

    private String status;

    @Enumerated(EnumType.STRING)
    private EtatValidation etatValidation = EtatValidation.INITIALE;

    private String garantiesTitre;
    private String garantiesValeur;
    private String garantiesFile; // PDF file path
    private String creditsFile; // PDF file path
    private String cautionsFile; // PDF file path
    private String lcFile; // PDF file path
    private String chequeFile; // PDF file path

    @ManyToOne
    @JoinColumn(name = "compte_id")
    private Compte compte;
    private LocalDateTime dateCreation;

 

   

    public enum EtatValidation {
        INITIALE,
        VALIDE,
        NON_VALIDE
    }
}