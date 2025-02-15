package com.bnm.recouvrement.dto;

import java.time.LocalDate;

import com.bnm.recouvrement.entity.Client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    private Integer nni;
    private String nif;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String secteurActivite;
    private String genre;
    private Double salaire;
    private String adresse;
    public ClientDTO(Client client) {
        this.nni = client.getNni();
        this.nom = client.getNom();
        this.prenom = client.getPrenom();
        this.adresse = client.getAdresse();
        this.secteurActivite = client.getSecteurActivite();
    }
}