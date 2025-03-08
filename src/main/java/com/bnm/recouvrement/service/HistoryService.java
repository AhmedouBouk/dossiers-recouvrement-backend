package com.bnm.recouvrement.service;

import com.bnm.recouvrement.entity.HistoryEvent;
import com.bnm.recouvrement.repository.HistoryEventRepository;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class HistoryService {

    private final HistoryEventRepository historyEventRepository;

    public HistoryService(HistoryEventRepository historyEventRepository) {
        this.historyEventRepository = historyEventRepository;
    }

    public List<HistoryEvent> getAllEvents() {
        return historyEventRepository.findAllByOrderByTimestampDesc();
    }

    public List<HistoryEvent> getEventsByUser(String username) {
        return historyEventRepository.findByUsernameOrderByTimestampDesc(username);
    }

    public List<HistoryEvent> getEventsByEntity(String entityType, String entityId) {
        return historyEventRepository.findByEntityTypeAndEntityIdOrderByTimestampDesc(entityType, entityId);
    }

    public HistoryEvent createEvent(String username, String action, String entityType, 
                                   String entityId, String entityName, String details) {
        HistoryEvent event = HistoryEvent.builder()
                .timestamp(new Date())
                .username(username)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .entityName(entityName)
                .details(details)
                .build();
        
        return historyEventRepository.save(event);
    }

    // Méthodes d'aide pour les actions courantes
    public HistoryEvent logImport(String username, String fileName, String details) {
        return createEvent(username, "import", "file", null, fileName, details);
    }

    public HistoryEvent logDelete(String username, String entityType, String entityId, String entityName) {
        return createEvent(username, "delete", entityType, entityId, entityName, null);
    }

    public HistoryEvent logCreate(String username, String entityType, String entityId, String entityName) {
        return createEvent(username, "create", entityType, entityId, entityName, null);
    }

    public HistoryEvent logUpdate(String username, String entityType, String entityId, String entityName) {
        return createEvent(username, "update", entityType, entityId, entityName, null);
    }
}
