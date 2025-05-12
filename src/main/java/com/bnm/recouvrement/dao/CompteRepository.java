package com.bnm.recouvrement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Compte;

@Repository
public interface CompteRepository extends JpaRepository<Compte, String> {
    Optional<Compte> findByNomCompte(String nomCompte);
     // Search by client's name
     List<Compte> findByClientNomContainingIgnoreCase(String nom);

     List<Compte> findByClientPrenomContainingIgnoreCase(String prenom);
 
     // Search by client's name and surname
     List<Compte> findByClientNomContainingIgnoreCaseAndClientPrenomContainingIgnoreCase(String nom, String prenom);
 
     // Search by client's NNI
     List<Compte> findByClientNni(Integer nni);
 
     // Combined search criteria
     List<Compte> findByClientNomContainingIgnoreCaseAndClientPrenomContainingIgnoreCaseAndClientNni(
             String nom, String prenom, Integer nni);
             
     @Query("SELECT c FROM Compte c JOIN FETCH c.client")
     List<Compte> findAllWithClient();
     
     @Query("SELECT c FROM Compte c JOIN c.client cli WHERE " +
            "LOWER(c.nomCompte) LIKE %:search% OR " +
            "LOWER(cli.nom) LIKE %:search% OR " +
            "LOWER(cli.prenom) LIKE %:search% OR " +
            "CAST(cli.nni AS string) LIKE %:search%")
     List<Compte> globalSearch(@Param("search") String search);
}
