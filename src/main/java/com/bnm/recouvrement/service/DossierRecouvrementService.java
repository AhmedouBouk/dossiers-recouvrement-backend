package com.bnm.recouvrement.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bnm.recouvrement.dao.CompteRepository;
import com.bnm.recouvrement.dao.DossierRecouvrementRepository;
import com.bnm.recouvrement.entity.Compte;
import com.bnm.recouvrement.entity.DossierRecouvrement;

@Service
public class DossierRecouvrementService {

    @Autowired
    private DossierRecouvrementRepository dossierRepository;

    @Autowired
    private CompteRepository compteRepository;

    public List<DossierRecouvrement> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public Optional<DossierRecouvrement> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    @Transactional
    public DossierRecouvrement createDossier(DossierRecouvrement dossier) {
        return dossierRepository.save(dossier);
    }

    @Transactional
    public DossierRecouvrement updateDossier(Long id, DossierRecouvrement dossier) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
        dossier.setId(id);
        return dossierRepository.save(dossier);
    }

    @Transactional
    public void deleteDossier(Long id) {
        if (!dossierRepository.existsById(id)) {
            throw new IllegalArgumentException("Dossier non trouvé avec l'ID: " + id);
        }
        dossierRepository.deleteById(id);
    }

    public List<DossierRecouvrement> searchDossiers(Long dossierId, String numeroCompte, String nomClient) {
        if (dossierId != null) {
            return dossierRepository.findById(dossierId)
                    .map(List::of)
                    .orElse(new ArrayList<>());
        } else if (numeroCompte != null) {
            return dossierRepository.findByCompteNomCompte(numeroCompte);
        } else if (nomClient != null) {
            return dossierRepository.findByCompteClientNomContainingIgnoreCase(nomClient);
        }
        return new ArrayList<>();
    }

    @Transactional
    public int importDossiersFromFile(String filePath) throws Exception {
        int importCount = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;

            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue;
                }

                String[] data = line.split(",");
                if (data.length < 14) {
                    throw new Exception("Format de ligne invalide: " + line);
                }

                String numeroCompte = data[0].trim();
                Optional<Compte> compteOpt = compteRepository.findById(numeroCompte);
                if (compteOpt.isEmpty()) {
                    throw new Exception("Compte non trouvé: " + numeroCompte);
                }

                DossierRecouvrement dossier = dossierRepository
                    .findByCompteNomCompte(numeroCompte)
                    .stream()
                    .findFirst()
                    .orElse(new DossierRecouvrement());

                dossier.setCompte(compteOpt.get());
                dossier.setEngagementTotal(parseDoubleOrNull(data[3]));
                dossier.setMontantPrincipal(parseDoubleOrNull(data[4]));
                dossier.setInteretContractuel(parseDoubleOrNull(data[5]));
                dossier.setInteretRetard(parseDoubleOrNull(data[6]));
                dossier.setAgenceOuvertureCompte(data[8]);
                dossier.setReferencesChecks(data[9]);
                dossier.setReferencesCredits(data[10]);
                dossier.setReferencesCautions(data[11]);
                dossier.setReferencesLC(data[12]);
                dossier.setProvision(parseDoubleOrNull(data[13]));
                dossier.setGarantiesValeur(data[14]);

                List<String> originesEngagement = new ArrayList<>();
                if (data[7] != null && !data[7].trim().isEmpty()) {
                    for (String origine : data[7].split(";")) {
                        originesEngagement.add(origine.trim());
                    }
                }
                dossier.setNaturesEngagement(originesEngagement);

                dossierRepository.save(dossier);
                importCount++;
            }
        }
        return importCount;
    }

    private Double parseDoubleOrNull(String value) {
        try {
            return value != null && !value.trim().isEmpty() ? Double.parseDouble(value.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}