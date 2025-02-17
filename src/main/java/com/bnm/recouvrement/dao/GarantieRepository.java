package com.bnm.recouvrement.dao;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Garantie;

@Repository
public interface GarantieRepository extends JpaRepository<Garantie, Long> {
    Optional<Garantie> findById(Long id);

}