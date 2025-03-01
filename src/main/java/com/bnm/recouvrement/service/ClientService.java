package com.bnm.recouvrement.service;


import com.bnm.recouvrement.dao.ClientRepository;
import com.bnm.recouvrement.entity.Client;

import io.jsonwebtoken.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    public void importClientsFromFile(String filePath) throws Exception {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean firstLine = true;
            
            while ((line = br.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    continue; // Ignorer l'en-tête
                }
                
                String[] data = line.split(",");
                try {
                    Integer nni = Integer.valueOf(data[0].trim());
                    String nif = data[1].trim();
                    String nom = data[2].trim();
                    String prenom = data[3].trim();
                    LocalDate dateNaissance = LocalDate.parse(data[4].trim());
                    String secteurActivite = data[5].trim();
                    String genre = data[6].trim();
                    Double salaire = Double.parseDouble(data[7].trim());
                    String adresse = data[8].trim();

                    Client client = new Client(nni, nif, nom, prenom, dateNaissance,
                            secteurActivite, genre, salaire, adresse);
                    
                    clientRepository.save(client);
                } catch (Exception e) {
                    throw new Exception("Erreur ligne " + line + ": " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new Exception("Erreur lors de la lecture du fichier: " + e.getMessage());
        }
    }
    @PreAuthorize("hasAuthority('READ_CLIENT')")
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    public void mettreAJourClient(Integer nni, String nom, String prenom, String adresse, Double salaire, String secteurActivite) {
        // Recherche du client par NNI
        Client client = clientRepository.findById(nni)
                .orElseThrow(() -> new IllegalArgumentException("Client non trouvé avec le NNI : " + nni));

        // Mise à jour des champs si des nouvelles valeurs sont fournies
        if (nom != null && !nom.isBlank()) {
            client.setNom(nom);
        }
        if (prenom != null && !prenom.isBlank()) {
            client.setPrenom(prenom);
        }
        if (adresse != null && !adresse.isBlank()) {
            client.setAdresse(adresse);
        }
        if (salaire != null) {
            client.setSalaire(salaire);
        }
        if (secteurActivite != null && !secteurActivite.isBlank()) {
            client.setSecteurActivite(secteurActivite);
        }

        // Sauvegarde du client mis à jour
        clientRepository.save(client);
    }
    public void supprimerClient(Integer nni) {
        // Vérifie si le client existe
        if (!clientRepository.existsById(nni)) {
            throw new IllegalArgumentException("Client non trouvé avec le NNI : " + nni);
        }
        // Supprime le client
        clientRepository.deleteById(nni);
    }
   
    public List<Client> rechercherClients(String nom, String prenom, Integer nni) {
        if (nni != null) {
            return clientRepository.findByNni(nni)
                    .map(List::of) // Wrap the client in a List if found
                    .orElseGet(List::of); // Return an empty List if not found
        } else if (nom != null && prenom != null) {
            return clientRepository.findByNomContainingIgnoreCaseAndPrenomContainingIgnoreCase(nom, prenom);
        } else if (nom != null) {
            return clientRepository.findByNomContainingIgnoreCase(nom);
        } else if (prenom != null) {
            return clientRepository.findByPrenomContainingIgnoreCase(prenom);
        } else {
            return List.of(); // Return an empty list if no criteria are provided
        }
    }

    @PreAuthorize("hasAuthority('READ_CLIENT')")
    public Optional<Client> getClientByNni(Integer nni) {
        return clientRepository.findByNni(nni);
    }
}
