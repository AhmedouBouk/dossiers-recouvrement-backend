package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dto.AgenceDto;
import com.bnm.recouvrement.entity.Agence;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenceService {

    private final AgenceRepository agenceRepository;

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
        return mapToDto(updatedAgence);
    }

    @Transactional
    public void deleteAgence(Long id) {
        if (!agenceRepository.existsById(id)) {
            throw new RuntimeException("Agence non trouvée avec l'ID: " + id);
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
        System.out.println(agenceDto);
        return Agence.builder()
                .id(agenceDto.getId())
                .code(agenceDto.getCode())
                .nom(agenceDto.getNom())
                .build();
    }
}
