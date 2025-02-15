package com.bnm.recouvrement.service;

import java.util.List;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.bnm.recouvrement.Config.JwtService;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dto.UpdateRequest;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final JwtService jwtService; 
    private final BCryptPasswordEncoder passwordEncoder;

    public User addUser(UserDto request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        try {
            Role userRole = Role.valueOf(request.getRole().toUpperCase());
            user.setRole(userRole);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role provided");
        }

        return userRepository.save(user);
    }

    public User updateUser(UpdateRequest request) {
        System.out.println("Hello from AdminService updateUser");
        User user = userRepository.findById(request.getId().intValue())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    
        user.setName(request.getName());
        user.setEmail(request.getEmail());
    
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
    
        if (request.getRole() != null) {
            try {
                Role userRole = Role.valueOf(request.getRole().toUpperCase());
                user.setRole(userRole);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role provided");
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
}