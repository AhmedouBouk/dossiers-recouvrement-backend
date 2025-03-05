package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dto.AgenceDto;
import com.bnm.recouvrement.dto.UserDetailsDto;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Agence;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AgenceRepository agenceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Integer id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<UserDto> getUsersByAgence(Long agenceId) {
        // Vérifier si l'agence existe
        Agence agence = agenceRepository.findById(agenceId)
                .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + agenceId));
        
        // Récupérer l'utilisateur actuel
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Si l'utilisateur est une AGENCE, vérifier qu'il appartient à l'agence demandée
        if ("AGENCE".equalsIgnoreCase(currentUser.getRole().getName()) && 
            (currentUser.getAgence() == null || !currentUser.getAgence().getId().equals(agenceId))) {
            throw new RuntimeException("Vous n'avez pas accès aux utilisateurs de cette agence");
        }
        
        // Récupérer tous les utilisateurs de l'agence
        return userRepository.findAll().stream()
                .filter(user -> user.getAgence() != null && user.getAgence().getId().equals(agenceId))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDetailsDto getCurrentUserDetails(User user) {
        // Convertir l'agence en DTO si elle existe
        AgenceDto agenceDto = null;
        if (user.getAgence() != null) {
            agenceDto = AgenceDto.builder()
                    .id(user.getAgence().getId())
                    .code(user.getAgence().getCode())
                    .nom(user.getAgence().getNom())
                    .build();
        }
        
        // Récupérer les permissions de l'utilisateur
        List<String> permissions = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        // Créer et retourner le DTO avec les détails de l'utilisateur
        return UserDetailsDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getName())
                .agence(agenceDto)
                .permissions(permissions)
                .build();
    }

    @Transactional
    public UserDto createUser(UserDto userDto) {
        // Vérifier si l'email est déjà utilisé
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        // Récupérer le rôle
        Role role = roleRepository.findByName(userDto.getRole())
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + userDto.getRole()));
        
        // Créer l'utilisateur
        User user = new User();
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(role);
        
        // Si un ID d'agence est fourni, associer l'utilisateur à l'agence
        if (userDto.getAgenceId() != null) {
            Agence agence = agenceRepository.findById(userDto.getAgenceId())
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + userDto.getAgenceId()));
            user.setAgence(agence);
        }
        
        // Si l'utilisateur a le rôle AGENCE, vérifier qu'une agence est associée
        if ("AGENCE".equalsIgnoreCase(role.getName()) && userDto.getAgenceId() == null) {
            throw new RuntimeException("Un utilisateur avec le rôle AGENCE doit être associé à une agence");
        }
        
        // Enregistrer l'utilisateur
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(Integer id, UserDto userDto) {
        // Récupérer l'utilisateur
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id));
        
        // Vérifier si l'email a changé et s'il est déjà utilisé
        if (!user.getEmail().equals(userDto.getEmail()) && 
            userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new RuntimeException("Un utilisateur avec cet email existe déjà");
        }
        
        // Récupérer le rôle si fourni
        if (userDto.getRole() != null) {
            Role role = roleRepository.findByName(userDto.getRole())
                    .orElseThrow(() -> new RuntimeException("Rôle non trouvé: " + userDto.getRole()));
            user.setRole(role);
            
            // Si l'utilisateur a le rôle AGENCE, vérifier qu'une agence est associée
            if ("AGENCE".equalsIgnoreCase(role.getName()) && 
                userDto.getAgenceId() == null && user.getAgence() == null) {
                throw new RuntimeException("Un utilisateur avec le rôle AGENCE doit être associé à une agence");
            }
        }
        
        // Mettre à jour les informations de l'utilisateur
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());
        
        // Mettre à jour le mot de passe si fourni
        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        }
        
        // Mettre à jour l'agence si fournie
        if (userDto.getAgenceId() != null) {
            Agence agence = agenceRepository.findById(userDto.getAgenceId())
                    .orElseThrow(() -> new RuntimeException("Agence non trouvée avec l'ID: " + userDto.getAgenceId()));
            user.setAgence(agence);
        }
        
        // Enregistrer les modifications
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer id) {
        // Vérifier si l'utilisateur existe
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("Utilisateur non trouvé avec l'ID: " + id);
        }
        
        // Supprimer l'utilisateur
        userRepository.deleteById(id);
    }

    private UserDto mapToDto(User user) {
        UserDto dto = new UserDto();
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().getName());
        
        if (user.getAgence() != null) {
            dto.setAgenceId(user.getAgence().getId());
        }
        
        return dto;
    }
}
