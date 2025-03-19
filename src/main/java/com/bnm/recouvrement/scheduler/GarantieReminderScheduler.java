package com.bnm.recouvrement.scheduler;

import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.DossierRecouvrement;
import com.bnm.recouvrement.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class GarantieReminderScheduler {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;
    
    @Autowired
    private NotificationService notificationService;
    
    // Exécuter tous les jours à 9h00
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendGarantieReminders() {
        // Trouver les dossiers qui nécessitent des garanties mais n'en ont pas encore
        List<DossierRecouvrement> dossiersSansGarantie = dossierRepository.findByGarantiesFileIsNull();
        
        for (DossierRecouvrement dossier : dossiersSansGarantie) {
            // Envoyer un rappel seulement si le dossier a une valeur de garantie spécifiée
            if (dossier.getGarantiesValeur() != null && !dossier.getGarantiesValeur().isEmpty()) {
                notificationService.notifyGarantieUploadRequired(dossier);
            }
        }
    }
}