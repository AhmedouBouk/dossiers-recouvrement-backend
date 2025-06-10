package com.bnm.recouvrement.dto;

import java.time.LocalDateTime;

public class CautionFileUpdateDto {
    private String title;
    private String cautionNumber;
    private Double montant;
    private LocalDateTime dateEcheance;

    // Getters and setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCautionNumber() {
        return cautionNumber;
    }

    public void setCautionNumber(String cautionNumber) {
        this.cautionNumber = cautionNumber;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public LocalDateTime getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDateTime dateEcheance) {
        this.dateEcheance = dateEcheance;
    }
}
