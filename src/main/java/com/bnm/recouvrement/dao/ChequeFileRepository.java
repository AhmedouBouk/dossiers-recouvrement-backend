package com.bnm.recouvrement.dao;

import com.bnm.recouvrement.entity.ChequeFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ChequeFileRepository extends JpaRepository<ChequeFile, Long> {
    List<ChequeFile> findByDossierId(Long dossierId);
    
    @Transactional
    void deleteByDossierId(Long dossierId);
}
