package com.bnm.recouvrement.loader;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;

@Component
public class DataLoader implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;

    @Autowired
    public DataLoader(PermissionRepository permissionRepository, RoleRepository roleRepository) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
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
    }
}