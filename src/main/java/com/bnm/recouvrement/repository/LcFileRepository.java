package com.bnm.recouvrement.repository;

import com.bnm.recouvrement.entity.LcFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LcFileRepository extends JpaRepository<LcFile, Long> {
    // Use explicit JPQL query to avoid property name issues
    @Query("SELECT lf FROM LcFile lf WHERE lf.dossierRecouvrement.id = :dossierId")
    List<LcFile> findByDossierRecouvrementId(@Param("dossierId") Long dossierId);
}
