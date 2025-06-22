package com.bnm.recouvrement.dao;

import com.bnm.recouvrement.entity.Garantie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface GarantieRepository extends JpaRepository<Garantie, Long> {
    List<Garantie> findByDossierId(Long dossierId);
    
    @Transactional
    void deleteByDossierId(Long dossierId);
}
