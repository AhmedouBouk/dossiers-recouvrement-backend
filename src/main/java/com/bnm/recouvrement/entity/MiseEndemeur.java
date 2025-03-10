package com.bnm.recouvrement.entity;


import java.time.LocalDate;
import lombok.Data;

@Data
public class MiseEndemeur {
    private Long id;
    private String reference;
    private LocalDate dateCreation;
    private String contenu;
    private Long dossierId;
    private String nomClient;
    private String prenomClient;
    private String adresseClient;
    private String numeroCompte;
    private Double montantDu;
    private String status; // ENVOYÉ, EN_ATTENTE, etc.
}