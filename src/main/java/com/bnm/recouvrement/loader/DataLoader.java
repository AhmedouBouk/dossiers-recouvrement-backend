package com.bnm.recouvrement.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;

@Component
public class DataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(PermissionRepository permissionRepository, RoleRepository roleRepository, 
                     UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("Démarrage de DataLoader pour initialiser les données...");
        // Créer les permissions par défaut
        List<String> defaultPermissions = List.of(
            // Permissions existantes
            "CREATE_ROLE", "ASSIGN_ROLE", "READ_USERS", "MANAGE_USERS",
            "READ_CLIENT", "UPDATE_CLIENT", "DELETE_CLIENT",
            "IMPORT_CLIENT", "CREATE_COMPTE", "READ_COMPTE", "UPDATE_COMPTE", "DELETE_COMPTE",
            "IMPORT_GRANTIE", "READ_GRANTIE", "UPDATE_GRANTIE", "DELETE_GRANTIE", "DOWNLOAD_GRANTIE",
            "IMPORT_CHEQUE", "READ_CHEQUE", "UPDATE_CHEQUE", "DELETE_CHEQUE", "DOWNLOAD_CHEQUE",
            "DETECT_DOSSIERRECOUVREMENT", "ADD_DOSSIERRECOUVREMENT", "UPDATE_DOSSIERRECOUVREMENT", 
         "DELETE_DOSSIERRECOUVREMENT", "DOWNLOAD_DOSSIERRECOUVREMENT",
            "MANAGE_AGENCE_USERS", "READ_AGENCE_USERS",
        
            // Permissions pour les crédits
            "CREATE_CREDIT", "READ_CREDIT", "UPDATE_CREDIT", "DELETE_CREDIT",
            "DOWNLOAD_CREDIT_FILE", "UPLOAD_CREDIT_FILE",
        
            // Permissions pour les lettres de crédit (LC)
            "CREATE_LC", "READ_LC", "UPDATE_LC", "DELETE_LC",
            "DOWNLOAD_LC_FILE", "UPLOAD_LC_FILE",
            "DOWNLOAD_MISE_EN_DEMEURE",
            "COMMENTAIRE_DOSSIER",
            "VALIDATION_DOSSIER",
            // Permissions pour les cautions
            "CREATE_CAUTION", "READ_CAUTION", "UPDATE_CAUTION", "DELETE_CAUTION",
            "DOWNLOAD_CAUTION_FILE", "UPLOAD_CAUTION_FILE"
        );
        for (String permName : defaultPermissions) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                permissionRepository.save(new Permission(null, permName));
            }
        }
        
        // Créer le rôle AGENCE s'il n'existe pas
        if (roleRepository.findByName("AGENCE").isEmpty()) {
            Role agenceRole = new Role();
            agenceRole.setName("AGENCE");
            
            // Attribuer les permissions au rôle AGENCE (uniquement visualisation)
            Set<Permission> agencePermissions = new HashSet<>();
            List<String> agencePermissionNames = List.of(
                "READ_CLIENT",
                "READ_COMPTE"
              
            );
            
            for (String permName : agencePermissionNames) {
                permissionRepository.findByName(permName).ifPresent(agencePermissions::add);
            }
            
            agenceRole.setPermissions(agencePermissions);
            roleRepository.save(agenceRole);
        }

        // Créer le rôle ADMIN s'il n'existe pas
        if (roleRepository.findByName("ADMIN").isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName("ADMIN");
            
            // Attribuer toutes les permissions au rôle ADMIN
            Set<Permission> adminPermissions = new HashSet<>();
            for (String permName : defaultPermissions) {
                permissionRepository.findByName(permName).ifPresent(adminPermissions::add);
            }
            
            adminRole.setPermissions(adminPermissions);
            roleRepository.save(adminRole);
        }
        
        // Créer l'utilisateur admin par défaut s'il n'existe pas
        String adminEmail = "admin@bnm.mr";
        log.info("Vérification de l'existence d'un utilisateur admin avec l'email: {}", adminEmail);
        Optional<User> existingAdmin = userRepository.findByEmail(adminEmail);
        
        if (existingAdmin.isPresent()) {
            log.info("Utilisateur admin existant trouvé: ID={}, Role={}", existingAdmin.get().getId(), existingAdmin.get().getRole().getName());
        } else {
            log.info("Aucun utilisateur admin existant trouvé avec l'email: {}. Création d'un nouvel utilisateur...", adminEmail);
            try {
                // Récupérer le rôle ADMIN
                Optional<Role> adminRole = roleRepository.findByName("ADMIN");
                
                if (adminRole.isPresent()) {
                    // S'assurer que le rôle a toutes les permissions nécessaires
                    Role role = adminRole.get();
                    
                    User adminUser = new User();
                    adminUser.setName("Admin");
                    adminUser.setEmail(adminEmail);
                    adminUser.setPassword(passwordEncoder.encode("123456"));
                    adminUser.setUserType("ADMIN");
                    adminUser.setRole(role);
                    
                    User savedUser = userRepository.save(adminUser);
                    log.info("Utilisateur admin créé avec succès : {}", adminUser.getEmail());
                    log.info("ID de l'utilisateur: {}", savedUser.getId());
                    log.info("Rôle de l'utilisateur: {}", savedUser.getRole().getName());
                } else {
                    log.error("Impossible de créer l'utilisateur admin: Rôle ADMIN introuvable.");
                }
            } catch (Exception e) {
                log.error("Erreur lors de la création de l'utilisateur admin", e);
            }
        }
        log.info("Fin de DataLoader.");
    }
}