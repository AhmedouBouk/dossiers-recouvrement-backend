package com.bnm.recouvrement.loader;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.entity.Permission;

@Component
public class DataLoader implements CommandLineRunner {
    private final PermissionRepository permissionRepository;

    @Autowired
    public DataLoader(PermissionRepository permissionRepository) {
        
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
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
}
