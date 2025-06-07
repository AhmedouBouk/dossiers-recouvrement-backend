package com.bnm.recouvrement.scheduler;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.NotificationService;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class GarantieReminderScheduler {

    private final DossierRecouvrementRepository dossierRepository;
    private final NotificationService notificationService;

    public GarantieReminderScheduler(DossierRecouvrementRepository dossierRepository, 
                                  NotificationService notificationService) {
        this.dossierRepository = dossierRepository;
        this.notificationService = notificationService;
    }
    
    // Exécuter tous les jours à 9h00
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendGarantieReminders() {
        // Trouver les dossiers qui nécessitent des garanties mais n'en ont pas encore
        // Updated to use the correct property name (filePath instead of file)
        List<DossierRecouvrement> dossiersSansGarantie = dossierRepository.findByGarantiesFilePathIsNull();
        
        for (DossierRecouvrement dossier : dossiersSansGarantie) {
            // Envoyer un rappel seulement si le dossier a une valeur de garantie spécifiée
            if (dossier.getGarantiesValeur() != null && !dossier.getGarantiesValeur().isEmpty()) {
                notificationService.notifyGarantieUploadRequired(dossier);
            }
        }
    }
}