package com.bnm.recouvrement.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString(exclude = "commentaires") // Exclut commentaires de toString()
public class DossierRecouvrement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private Double engagementTotal;
    private Double montantPrincipal;
    private Double interetContractuel;
    private Double interetRetard;

 
    @Column(name = "nature" ,columnDefinition = "TEXT")
    private String naturesEngagement; // credit, debit, cautions

    private String agenceOuvertureCompte;
    private String referencesChecks; // Format: TT/CodeAgence/NumeroCheck
    private String referencesCredits; // MG
    private String referencesCautions;
    private String referencesLC; // Lettres de Crédit
    private Double provision;
    private Double interetsReserves;

  @Enumerated(EnumType.STRING)
    private Status status = Status.EN_COURS; // Valeur par défaut
    

    @Enumerated(EnumType.STRING)
    private EtatValidation etatValidation = EtatValidation.INITIALE;

    private String garantiesValeur;
    
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<Garantie> garanties = new ArrayList<>();
    
    @OneToMany(mappedBy = "dossier", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    private List<ChequeFile> chequeFiles = new ArrayList<>();
    
    private String creditsFile; // PDF file path
    private String cautionsFile; // PDF file path
    private String lcFile; // PDF file path
    private String chequeFile; // PDF file path (legacy - kept for backward compatibility)

    @ManyToOne
    @JoinColumn(name = "compte_id")
    private Compte compte;
    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "dossier", cascade = CascadeType.PERSIST)
    @JsonIgnore // Empêche la sérialisation JSON récursive
    private List<Comment> commentaires = new ArrayList<>();

 

  @Column(name = "date_archivage")
    private LocalDateTime dateArchivage;

    public LocalDateTime getDateArchivage() {
        return dateArchivage;
    }

    public void setDateArchivage(LocalDateTime dateArchivage) {
        this.dateArchivage = dateArchivage;
    }


    public enum EtatValidation {
        INITIALE,
        COMPLET,

        VALIDE,
        NON_VALIDE
    }
    

public enum Status {
    EN_COURS,  ARCHIVEE
}


}