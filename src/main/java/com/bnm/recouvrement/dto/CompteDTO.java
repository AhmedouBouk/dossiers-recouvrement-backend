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
}