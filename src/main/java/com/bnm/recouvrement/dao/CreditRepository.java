package com.bnm.recouvrement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Credit;

@Repository
public interface CreditRepository extends JpaRepository<Credit, Long> {
    Optional<Credit> findById(Long id);
    List<Credit> findByCompteNomCompte(String nomCompte);

}