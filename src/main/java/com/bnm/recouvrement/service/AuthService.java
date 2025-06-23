package com.bnm.recouvrement.service;

import com.bnm.recouvrement.Config.JwtService;
import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dto.AuthRequest;
import com.bnm.recouvrement.dto.AuthResponse;
import com.bnm.recouvrement.dto.PasswordChangeRequest;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Agence;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgenceRepository agenceRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;
    private final HistoryService historyService;

    public AuthResponse register(UserDto request) {
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        
        // Si l'utilisateur a le rôle AGENCE, vérifier qu'une agence est associée
        if ("AGENCE".equalsIgnoreCase(role.getName()) && request.getAgenceId() == null) {
            throw new RuntimeException("Un utilisateur avec le rôle AGENCE doit être associé à une agence");
        }
        
        // Si un ID d'agence est fourni, associer l'utilisateur à l'agence
        if (request.getAgenceId() != null) {
            Agence agence = agenceRepository.findById(request.getAgenceId())
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + request.getAgenceId()));
            user.setAgence(agence);
        }

        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user));
    }

    public AuthResponse authenticate(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPassword()));

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Vérifier si le rôle de l'utilisateur est actif
        if (user.getRole() == null) { 
            throw new BadCredentialsException("User has no assigned role."); 
        }
        if (!user.getRole().isActive()) {
            throw new DisabledException("The account's role ('" + user.getRole().getName() + "') is currently deactivated. Please contact an administrator.");
        }

        
        // Enregistrer l'événement de connexion dans l'historique
        String details = "Connexion réussie";
        if (user.getRole() != null) {
            details += " - Rôle: " + user.getRole().getName();
        }
        if (user.getAgence() != null) {
            details += " - Agence: " + user.getAgence().getNom();
        }
        historyService.createEvent(
            user.getEmail(),
            "LOGIN",
            "USER",
            user.getId() != null ? user.getId().toString() : null,
            user.getName(),
            details
        );

        AuthResponse response = new AuthResponse();
        response.setToken(jwtService.generateToken(user));
        response.setFirstLogin(user.isFirstLogin());
        
        return response;
    }
    
    public void changePassword(String email, PasswordChangeRequest request) {
        // Validate password match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadCredentialsException("Les mots de passe ne correspondent pas");
        }
        
        // Find user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé"));
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        
        // Update firstLogin flag
        user.setFirstLogin(false);
        
        // Save user
        userRepository.save(user);
        
        // Log password change event
        historyService.createEvent(
            user.getEmail(),
            "PASSWORD_CHANGE",
            "USER",
            user.getId() != null ? user.getId().toString() : null,
            user.getName(),
            "Mot de passe modifié avec succès"
        );
    }
}
