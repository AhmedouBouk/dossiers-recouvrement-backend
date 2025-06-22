package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.CreditFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditFileRepository extends JpaRepository<CreditFile, Long> {
    List<CreditFile> findByDossierId(Long dossierId);
}
