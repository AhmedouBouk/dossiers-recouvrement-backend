package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.dto.AssignRoleRequest;
import com.bnm.recouvrement.dto.RoleRequest;
import com.bnm.recouvrement.dto.UpdateRequest;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor

@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/roles")
    public ResponseEntity<?> createRole(@RequestBody RoleRequest roleRequest) {
        System.out.println(" Received Role Data: " + roleRequest.getRoleName());
        System.out.println(" Received Permissions: " + roleRequest.getPermissions());

        if (roleRequest.getRoleName() == null || roleRequest.getRoleName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name cannot be empty!");
        }

        if (roleRequest.getPermissions() == null || roleRequest.getPermissions().isEmpty()) {
            return ResponseEntity.badRequest().body("Permissions cannot be empty!");
        }

        try {
            Role createdRole = adminService.createRole(roleRequest);
            return ResponseEntity.ok(createdRole);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/assign-role")
    public ResponseEntity<User> assignRoleToUser(@RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(adminService.assignRoleToUser(request));
    }

    @GetMapping("/roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @GetMapping("/permissions")
    public ResponseEntity<List<Permission>> getAllPermissions() {
        return ResponseEntity.ok(adminService.getAllPermissions());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = adminService.getAllUsers();
            for( User user : adminService.getAllUsers()) {
                System.out.println(user.getEmail());
            }
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Integer id) {
        try {
            User user = adminService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/add")
    public ResponseEntity<User> addUser(@RequestBody UserDto request) {
        try {
            System.out.println("Received user request: " + request);
            User newUser = adminService.addUser(request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            System.err.println("Error adding user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/update")
    public ResponseEntity<User> updateUser(@RequestBody UpdateRequest request) {
        try {
            System.out.println("Hello from AdminController updateUser");
            User updatedUser = adminService.updateUser(request);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        try {
            adminService.deleteUserById(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    @PutMapping("/roles/{id}")
    public ResponseEntity<?> updateRole(@PathVariable Long id, @RequestBody RoleRequest roleRequest) {
        if (roleRequest.getRoleName() == null || roleRequest.getRoleName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Role name cannot be empty!");
        }

        if (roleRequest.getPermissions() == null || roleRequest.getPermissions().isEmpty()) {
            return ResponseEntity.badRequest().body("Permissions cannot be empty!");
        }

        try {
            Role updatedRole = adminService.updateRole(id, roleRequest);
            return ResponseEntity.ok(updatedRole);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/roles/{id}/toggle-status")
    public ResponseEntity<?> toggleRoleStatus(@PathVariable Long id) {
        try {
            Role updatedRole = adminService.toggleRoleStatus(id);
            return ResponseEntity.ok(updatedRole);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
