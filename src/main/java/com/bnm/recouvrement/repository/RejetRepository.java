package com.bnm.recouvrement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.entity.Rejet;

@Repository
public interface RejetRepository extends JpaRepository<Rejet, Long> {
    
    /**
     * Trouve tous les rejets pour un dossier spécifique
     * @param dossier Le dossier concerné
     * @return Liste des rejets pour ce dossier
     */
    List<Rejet> findByDossierOrderByDateRejetDesc(DossierRecouvrement dossier);
    
    /**
     * Trouve tous les rejets non traités
     * @return Liste des rejets non traités
     */
    List<Rejet> findByTraiteOrderByDateRejetDesc(boolean traite);
    
    /**
     * Trouve tous les rejets pour un type d'utilisateur spécifique
     * @param typeUtilisateur Le type d'utilisateur
     * @return Liste des rejets concernant ce type d'utilisateur
     */
    List<Rejet> findByTypesUtilisateursContainingOrderByDateRejetDesc(String typeUtilisateur);
}
