package com.bnm.recouvrement.service;


import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


import com.bnm.recouvrement.Config.JwtService;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dto.AuthRequest;
import com.bnm.recouvrement.dto.AuthResponse;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    // Authenticate a user and return a JWT token
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getEmail(), authRequest.getPassword()
                )
            );
            System.out.println("hello from service this is 2");
        } catch (Exception e) {
            System.out.println("hello this has failed from autservice");
            throw new RuntimeException("Authentication failed", e);
            
        }

        User user = userRepository.findByEmail(authRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        String jwtToken = jwtService.generateToken(user);
        System.out.println(jwtToken);
        return new AuthResponse(jwtToken);
    }

    // Register a new user and return a JWT token
    public AuthResponse register(UserDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // Hash password

        try {
            Role userRole = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role provided");
        }

        userRepository.save(user);
        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken);
    }

    // Update an existing user and return an updated JWT token
   

   
}