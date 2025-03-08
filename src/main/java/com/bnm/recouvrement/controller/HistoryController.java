package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.entity.HistoryEvent;
import com.bnm.recouvrement.service.HistoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<List<HistoryEvent>> getAllEvents() {
        return ResponseEntity.ok(historyService.getAllEvents());
    }

    @GetMapping("/user/{username}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<List<HistoryEvent>> getEventsByUser(@PathVariable String username) {
        return ResponseEntity.ok(historyService.getEventsByUser(username));
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<List<HistoryEvent>> getEventsByEntity(
            @PathVariable String entityType, 
            @PathVariable String entityId) {
        return ResponseEntity.ok(historyService.getEventsByEntity(entityType, entityId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<HistoryEvent> createEvent(@RequestBody Map<String, String> eventData) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        HistoryEvent event = historyService.createEvent(
            username,
            eventData.get("action"),
            eventData.get("entityType"),
            eventData.get("entityId"),
            eventData.get("entityName"),
            eventData.get("details")
        );
        
        return ResponseEntity.ok(event);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<HistoryEvent> logImport(@RequestBody Map<String, String> data) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        HistoryEvent event = historyService.logImport(
            username,
            data.get("fileName"),
            data.get("details")
        );
        
        return ResponseEntity.ok(event);
    }

    @PostMapping("/delete")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
    public ResponseEntity<HistoryEvent> logDelete(@RequestBody Map<String, String> data) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        HistoryEvent event = historyService.logDelete(
            username,
            data.get("entityType"),
            data.get("entityId"),
            data.get("entityName")
        );
        
        return ResponseEntity.ok(event);
    }
}
