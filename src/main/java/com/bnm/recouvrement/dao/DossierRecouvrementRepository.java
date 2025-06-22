package com.bnm.recouvrement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.DossierRecouvrement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


@Repository
public interface DossierRecouvrementRepository extends JpaRepository<DossierRecouvrement, Long> {

    List<DossierRecouvrement> findByCompteNomCompte(String numeroCompte);
    // Basic CRUD operations are provided by JpaRepository

    List<DossierRecouvrement> findByCompteClientNomContainingIgnoreCase(String nomClient);

    // Updated to use the correct property name (filePath instead of file)
    List<DossierRecouvrement> findByGarantiesFilePathIsNull();



    List<DossierRecouvrement> findByEtatValidation(DossierRecouvrement.EtatValidation etatValidation);

/**
 * Trouve tous les dossiers SAUF ceux avec l'état spécifié
 */
List<DossierRecouvrement> findByEtatValidationNot(DossierRecouvrement.EtatValidation etatValidation);

/**
 * Compte les dossiers par état de validation
 */
long countByEtatValidation(DossierRecouvrement.EtatValidation etatValidation);

/**
 * Trouve tous les dossiers archivés avec pagination
 */
Page<DossierRecouvrement> findByEtatValidation(DossierRecouvrement.EtatValidation etatValidation, Pageable pageable);

/**
 * Trouve tous les dossiers actifs (non archivés) avec pagination
 */
Page<DossierRecouvrement> findByEtatValidationNot(DossierRecouvrement.EtatValidation etatValidation, Pageable pageable);



 List<DossierRecouvrement> findByStatus(DossierRecouvrement.Status status);
    
    List<DossierRecouvrement> findByStatusNot(DossierRecouvrement.Status status);
    
    long countByStatus(DossierRecouvrement.Status status);
}