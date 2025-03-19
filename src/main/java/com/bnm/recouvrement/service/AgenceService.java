package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dto.AgenceDto;
import com.bnm.recouvrement.entity.Agence;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenceService {

    private final AgenceRepository agenceRepository;
    
    @Autowired
    private HistoryService historyService;

    @Transactional(readOnly = true)
    public List<AgenceDto> getAllAgences() {
        return agenceRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgenceDto getAgenceById(Long id) {
        return agenceRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + id));
    }

    @Transactional
    public AgenceDto createAgence(AgenceDto agenceDto) {
        if (agenceRepository.existsByCode(agenceDto.getCode())) {
            throw new RuntimeException("Une agence avec ce code existe déjà");
        }
        
        Agence agence = mapToEntity(agenceDto);
        Agence savedAgence = agenceRepository.save(agence);
        
        // Enregistrer l'événement de création dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logCreate(
            username, 
            "agence", 
            savedAgence.getId().toString(), 
            "Agence " + savedAgence.getNom() + " (" + savedAgence.getCode() + ")"
        );
        
        return mapToDto(savedAgence);
    }

    @Transactional
    public AgenceDto updateAgence(Long id, AgenceDto agenceDto) {
        Agence agence = agenceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + id));
        
        // Vérifier si le code a changé et s'il est déjà utilisé
        if (!agence.getCode().equals(agenceDto.getCode()) && 
            agenceRepository.existsByCode(agenceDto.getCode())) {
            throw new RuntimeException("Une agence avec ce code existe déjà");
        }
        
        agence.setCode(agenceDto.getCode());
        agence.setNom(agenceDto.getNom());
        
        Agence updatedAgence = agenceRepository.save(agence);
        
        // Enregistrer l'événement de mise à jour dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        historyService.logUpdate(
            username, 
            "agence", 
            updatedAgence.getId().toString(), 
            "Agence " + updatedAgence.getNom() + " (" + updatedAgence.getCode() + ")"
        );
        
        return mapToDto(updatedAgence);
    }

    @Transactional
    public void deleteAgence(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new RuntimeException("Agence non trouvée avec l'ID: " + id);
        }
        
        // Récupérer l'agence avant de la supprimer pour l'historique
        Agence agence = agenceRepository.findById(id).orElse(null);
        
        // Enregistrer l'événement de suppression dans l'historique
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        if (agence != null) {
            historyService.logDelete(
                username, 
                "agence", 
                id.toString(), 
                "Agence " + agence.getNom() + " (" + agence.getCode() + ")"
            );
        }
        
        agenceRepository.deleteById(id);
    }

    private AgenceDto mapToDto(Agence agence) {
        return AgenceDto.builder()
                .id(agence.getId())
                .code(agence.getCode())
                .nom(agence.getNom())
                .build();
    }

    private Agence mapToEntity(AgenceDto agenceDto) {
        return Agence.builder()
                .id(agenceDto.getId())
                .code(agenceDto.getCode())
                .nom(agenceDto.getNom())
                .build();
    }
}