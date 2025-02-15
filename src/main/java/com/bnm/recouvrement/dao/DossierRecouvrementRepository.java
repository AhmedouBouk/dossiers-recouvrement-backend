package com.bnm.recouvrement.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.DossierRecouvrement;

@Repository
public interface DossierRecouvrementRepository extends JpaRepository<DossierRecouvrement, Long> {
    Optional<DossierRecouvrement> findById(Long id);

    Optional<DossierRecouvrement> findByAccountNumber(String accountNumber);

	List<DossierRecouvrement> findByStatus(String Status);
    

}