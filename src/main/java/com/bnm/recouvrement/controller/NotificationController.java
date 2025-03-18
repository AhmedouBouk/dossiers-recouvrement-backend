package com.bnm.recouvrement.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.repository.*;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.Notification;
import com.bnm.recouvrement.entity.User;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id) {
        // Récupérer l'utilisateur actuellement connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer la notification
        Optional<Notification> notificationOpt = notificationRepository.findById(id);
        
        if (notificationOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Notification notification = notificationOpt.get();
        
        // Vérifier que la notification appartient à l'utilisateur connecté
        if (!notification.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(403).body("Vous n'avez pas l'autorisation de modifier cette notification");
        }
        
        // Marquer comme lue
        notification.setLu(true);
        notificationRepository.save(notification);
        
        return ResponseEntity.ok().build();
    }
}