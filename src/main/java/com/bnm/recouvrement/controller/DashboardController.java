package com.bnm.recouvrement.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bnm.recouvrement.dao.ClientRepository;
import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;

@RestController
@RequestMapping("/dashboard")
public class DashboardController<DossierRepository> {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private CompteRepository compteRepository;

    @Autowired
    private ClientRepository clientRepository;

    

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDossiers", dossierRepository.count());
        stats.put("totalComptes", compteRepository.count());
        stats.put("totalClients", clientRepository.count());
        return ResponseEntity.ok(stats);
    }
}

