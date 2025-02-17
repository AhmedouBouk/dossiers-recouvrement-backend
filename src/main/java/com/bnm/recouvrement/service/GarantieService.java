package com.bnm.recouvrement.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bnm.recouvrement.dao.GarantieRepository;
import com.bnm.recouvrement.entity.Garantie;

@Service
public class GarantieService {

    @Autowired
    private GarantieRepository garantieRepository;

    // Créer une garantie
    public Garantie creerGarantie( Garantie garantie) throws IOException {

        return garantieRepository.save(garantie);
    }
    // Lire une garantie par son ID
    public Optional<Garantie> lireGarantie(Long id) {
        return garantieRepository.findById(id);
    }

    // Lire toutes les garanties
    public List<Garantie> lireToutesLesGaranties() {
        return garantieRepository.findAll();
    }

    // Mettre à jour une garantie existante
    public Garantie mettreAJourGarantie(Long id, Garantie garantieMiseAJour) {
        Garantie garantie = garantieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Garantie non trouvée pour l'id : " + id));

        garantie.setTypeGarantie(garantieMiseAJour.getTypeGarantie());
        garantie.setValeur(garantieMiseAJour.getValeur());
        garantie.setDescription(garantieMiseAJour.getDescription());
        garantie.setFondDossier(garantieMiseAJour.getFondDossier());

        return garantieRepository.save(garantie);
    }

    // Supprimer une garantie
    public void supprimerGarantie(Long id) {
        if (!garantieRepository.existsById(id)) {
            throw new RuntimeException("Garantie non trouvée pour l'id : " + id);
        }
        garantieRepository.deleteById(id);
    }
    
}