package com.bnm.recouvrement.loader;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;

@Component
public class DataLoader implements CommandLineRunner {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataLoader(PermissionRepository permissionRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.permissionRepository = permissionRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Charger les permissions par défaut
        loadDefaultPermissions();

        // Créer l'utilisateur ADMIN si nécessaire
        createAdminUser();
    }

    private void loadDefaultPermissions() {
        List<String> defaultPermissions = List.of(
            "CREATE_ROLE", "ASSIGN_ROLE", "READ_USERS", "MANAGE_USERS",
            "READ_CLIENT", "UPDATE_CLIENT", "DELETE_CLIENT",
            "IMPORT_CLIENT", "CREATE_COMPTE", "READ_COMPTE", "UPDATE_COMPTE", "DELETE_COMPTE",
            "CREATE_CREDIT", "READ_CREDIT", "UPDATE_CREDIT", "DELETE_CREDIT", "DOWNLOAD_CREDIT",
            "IMPORT_CREDIT",
            "DETECT_IMPAYES", "ADD_CREDIT_TO_DOSSIER", "UPDATE_DOSSIER", "MODIFY_DOSSIER_STATUS", "DELETE_DOSSIER", "DOWNLOAD_DOSSIER"
        );

        for (String permName : defaultPermissions) {
            if (permissionRepository.findByName(permName).isEmpty()) {
                permissionRepository.save(new Permission(null, permName));
            }
        }
    }

    private void createAdminUser() {
        // Vérifier si le rôle ADMIN existe déjà
        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> {
                    Role newRole = new Role();
                    newRole.setName("ADMIN");
                    return roleRepository.save(newRole);
                });

        // Vérifier si l'utilisateur ADMIN existe déjà
        if (!userRepository.findByEmail("23018@esp.com").isPresent()) {
            User adminUser = new User();
            adminUser.setName("Admin");
            adminUser.setEmail("23018@esp.com");
            adminUser.setPassword(passwordEncoder.encode("12345")); // Encoder le mot de passe
            adminUser.setRole(adminRole);

            userRepository.save(adminUser);
            System.out.println("Admin user created successfully!");
        } else {
            System.out.println("Admin user already exists.");
        }
    }
}