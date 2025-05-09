package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'AGENCE')")
public class UserController {
    private final AdminService adminService;

    /**
     * Endpoint pour créer un nouvel utilisateur
     * Accessible aux rôles ADMIN et AGENCE
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserDto request) {
        try {
            System.out.println("UserController - Received user creation request: " + request);
            System.out.println("UserController - User type: " + request.getUserType());
            System.out.println("UserController - Agency ID: " + request.getAgenceId());
            
            // Validation des champs obligatoires
            List<String> missingFields = new ArrayList<>();
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                missingFields.add("email");
            } else if (!isValidEmail(request.getEmail())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Format d'email invalide"));
            }
            
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                missingFields.add("nom");
            }
            
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                missingFields.add("mot de passe");
            } else if (request.getPassword().length() < 8) { 
                return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit contenir au moins 8 caractères"));
            } else {
                // Ajout de la validation pour le chiffre et le caractère spécial
                String passwordPattern = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$";
                if (!request.getPassword().matches(passwordPattern)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Le mot de passe doit contenir au moins un chiffre et un caractère spécial."));
                }
            }
            
            if (!missingFields.isEmpty()) {
                String errorMessage = "Champs obligatoires manquants : " + String.join(", ", missingFields);
                return ResponseEntity.badRequest().body(Map.of("error", errorMessage));
            }
            
            User newUser = adminService.addUser(request);
            return ResponseEntity.ok(newUser);
        } catch (IllegalStateException e) {
            // Utilisateur déjà existant
            System.err.println("UserController - Error creating user: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", "Cet email est déjà utilisé par un autre utilisateur"));
        } catch (IllegalArgumentException e) {
            // Erreur de validation
            System.err.println("UserController - Validation error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            // Autres erreurs
            System.err.println("UserController - Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Une erreur est survenue lors de la création de l'utilisateur"));
        }
    }
    
    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        return pattern.matcher(email).matches();
    }
}
