package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dao.AgenceRepository;
import com.bnm.recouvrement.dto.AssignRoleRequest;
import com.bnm.recouvrement.dto.RoleRequest;
import com.bnm.recouvrement.dto.UpdateRequest;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.entity.Agence;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final AgenceRepository agenceRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Create a new role with selected permissions
    public Role createRole(RoleRequest request) {
        if (roleRepository.findByName(request.getRoleName()).isPresent()) {
            throw new RuntimeException("Role already exists: " + request.getRoleName());
        }

        Set<Permission> permissions = request.getPermissions().stream()
                .map(permissionName -> permissionRepository.findByName(permissionName)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName)))
                .collect(Collectors.toSet());

        Role role = new Role();
        role.setName(request.getRoleName());
        role.setPermissions(permissions);

        return roleRepository.save(role);
    }

    // Assign a role to a user
    public User assignRoleToUser(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + request.getUserId()));

        Role role = roleRepository.findByName(request.getRoleName())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRoleName()));

        user.setRole(role);
        return userRepository.save(user);
    }

    // Get all roles
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    // Get all permissions
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    // CRUD for users
    public User updateUser(UpdateRequest request) {
        User user = userRepository.findById(request.getId().intValue())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRole() != null) {
            Role role = roleRepository.findByName(request.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }
        if (request.getUserType() != null) {
            user.setUserType(request.getUserType());
            
            // Si le type d'utilisateur est Agence, vérifier l'association à une agence
            if ("Agence".equals(request.getUserType()) && request.getAgenceId() != null) {
                Agence agence = agenceRepository.findById(request.getAgenceId())
                        .orElseThrow(() -> new RuntimeException("Agence not found with ID: " + request.getAgenceId()));
                user.setAgence(agence);
            }
        }
        return userRepository.save(user);
    }

    public void deleteUserById(Integer id) {
        if (!userRepository.existsById(id)) {
            throw new UsernameNotFoundException("User not found with ID: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + id));
    }

    // Admin adds a new user (with dynamic role)
    public User addUser(UserDto request) {
        // Validation des champs obligatoires
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("L'email est obligatoire");
        }
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom est obligatoire");
        }
        
        // Validation du mot de passe
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire");
        }
        
        if (request.getPassword().length() < 8) { 
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Ajout de la validation pour le chiffre et le caractère spécial
        String passwordPattern = "^(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).*$";
        if (!request.getPassword().matches(passwordPattern)) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins un chiffre et un caractère spécial.");
        }
        
        // Vérification si l'utilisateur existe déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Cet email est déjà utilisé par un autre utilisateur");
        }
        
        System.out.println("Adding user with email: " + request.getEmail());
        System.out.println("Password length before encoding: " + request.getPassword().length());
        System.out.println("Role: " + request.getRole());
        System.out.println("AgenceId: " + request.getAgenceId());
        System.out.println("UserType: " + request.getUserType());
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        
        // Encodage du mot de passe avec vérification
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        System.out.println("Encoded password: " + encodedPassword);
        user.setPassword(encodedPassword);
        
        // Use dynamic lookup rather than enum conversion
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
        user.setRole(role);
        
        // Définir le type d'utilisateur
        user.setUserType(request.getUserType());
        
        // Si le rôle est AGENCE et qu'un ID d'agence est fourni, associer l'utilisateur à cette agence
        if ("AGENCE".equalsIgnoreCase(request.getRole()) && request.getAgenceId() != null) {
            Agence agence = agenceRepository.findById(request.getAgenceId())
                    .orElseThrow(() -> new RuntimeException("Agence not found with ID: " + request.getAgenceId()));
            user.setAgence(agence);
            System.out.println("User associated with agency: " + agence.getNom());
        }
        
        // Si le type d'utilisateur est Agence, associer à l'agence correspondante
        if ("Agence".equals(request.getUserType()) && request.getAgenceId() != null) {
            Agence agence = agenceRepository.findById(request.getAgenceId())
                    .orElseThrow(() -> new RuntimeException("Agence not found with ID: " + request.getAgenceId()));
            user.setAgence(agence);
            System.out.println("User of type Agence associated with agency: " + agence.getNom());
        }
        
        return userRepository.save(user);
    }
    // Update an existing role
public Role updateRole(Long roleId, RoleRequest request) {
    Role role = roleRepository.findById(roleId)
            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
    
    // Check if another role with the same name exists (excluding the current role)
    roleRepository.findByName(request.getRoleName())
            .ifPresent(existingRole -> {
                if (!existingRole.getId().equals(roleId)) {
                    throw new RuntimeException("Another role with name '" + request.getRoleName() + "' already exists");
                }
            });
    
    Set<Permission> permissions = request.getPermissions().stream()
            .map(permissionName -> permissionRepository.findByName(permissionName)
                    .orElseThrow(() -> new RuntimeException("Permission not found: " + permissionName)))
            .collect(Collectors.toSet());
    
    role.setName(request.getRoleName());
    role.setPermissions(permissions);
    
    return roleRepository.save(role);
}

    // Toggle active status of an existing role
    public Role toggleRoleStatus(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));
        
        role.setActive(!role.isActive()); // Inverse le statut actuel
        
        return roleRepository.save(role);
    }
}
