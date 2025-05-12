package com.bnm.recouvrement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Client;

@Repository
public interface ClientRepository extends JpaRepository<Client, Integer> {
    Optional<Client> findByNni(Integer nni);
    
    List<Client> findByPrenomContainingIgnoreCase(String prenom);

    List<Client> findByNomContainingIgnoreCaseAndPrenomContainingIgnoreCase(String nom, String prenom);

    List<Client> findByNomContainingIgnoreCaseAndPrenomContainingIgnoreCaseAndNni(String nom, String prenom, Integer nni);

    List<Client> findByNomContainingIgnoreCase(String nom);
    
    @Query("SELECT c FROM Client c WHERE " +
           "CAST(c.nni AS string) LIKE %:search% OR " +
           "LOWER(c.nom) LIKE %:search% OR " +
           "LOWER(c.prenom) LIKE %:search%")
    List<Client> globalSearch(@Param("search") String search);
}
