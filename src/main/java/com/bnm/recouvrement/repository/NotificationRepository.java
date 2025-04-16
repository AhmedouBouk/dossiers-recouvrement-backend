package com.bnm.recouvrement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDateCreationDesc(Integer userId);
    
    List<Notification> findByUserIdAndLuOrderByDateCreationDesc(Long userId, boolean lu);
    void deleteByDossierId(Long dossierId);

    
    long countByUserIdAndLu(Long userId, boolean lu);
}