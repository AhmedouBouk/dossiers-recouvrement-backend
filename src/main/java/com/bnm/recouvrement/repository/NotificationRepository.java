package com.bnm.recouvrement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bnm.recouvrement.entity.Notification;
import com.bnm.recouvrement.entity.User;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserIdOrderByDateCreationDesc(Integer userId);
    
    List<Notification> findByUserIdAndLuOrderByDateCreationDesc(Long userId, boolean lu);
    void deleteByDossierId(Long dossierId);
    
    /**
     * Trouve les notifications d'un utilisateur selon leur statut de lecture
     * @param user L'utilisateur concerné
     * @param lu Le statut de lecture (true = lue, false = non lue)
     * @return La liste des notifications triées par date de création décroissante
     */
    List<Notification> findByUserAndLuOrderByDateCreationDesc(User user, boolean lu);
    
    long countByUserIdAndLu(Long userId, boolean lu);
}