package com.bnm.recouvrement.controller;

import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.User;
import com.bnm.recouvrement.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<User> createUser(@RequestBody UserDto request) {
        try {
            System.out.println("UserController - Received user creation request: " + request);
            System.out.println("UserController - User type: " + request.getUserType());
            System.out.println("UserController - Agency ID: " + request.getAgenceId());
            
            User newUser = adminService.addUser(request);
            return ResponseEntity.ok(newUser);
        } catch (Exception e) {
            System.err.println("UserController - Error creating user: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
