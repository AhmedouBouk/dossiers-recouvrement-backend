package com.bnm.recouvrement.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.DossierRecouvrement;

@Repository
public interface DossierRecouvrementRepository extends JpaRepository<DossierRecouvrement, Long> {

    List<DossierRecouvrement> findByCompteNomCompte(String numeroCompte);
    // Basic CRUD operations are provided by JpaRepository

    List<DossierRecouvrement> findByCompteClientNomContainingIgnoreCase(String nomClient);

    List<DossierRecouvrement> findByGarantiesFileIsNull();
}