package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.CautionFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CautionFileRepository extends JpaRepository<CautionFile, Long> {
    List<CautionFile> findByDossierId(Long dossierId);
}
