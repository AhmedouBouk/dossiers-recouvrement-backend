package com.bnm.recouvrement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.dao.ClientRepository;
import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.repository.NotificationRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.Notification;
import com.bnm.recouvrement.entity.User;

@RestController
@RequestMapping("/dashboard")
public class DashboardController<DossierRepository> {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDossiers", dossierRepository.count());
        stats.put("totalComptes", compteRepository.count());
        stats.put("totalClients", clientRepository.count());
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getUserNotifications() {
        // Récupérer l'utilisateur actuellement connecté
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // Nom d'utilisateur (email dans notre cas)
        
        // Trouver l'utilisateur par email
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        // Récupérer les notifications pour cet utilisateur
        List<Notification> notifications = notificationRepository.findByUserIdOrderByDateCreationDesc(currentUser.getId());
        
        return ResponseEntity.ok(notifications);
    }
}