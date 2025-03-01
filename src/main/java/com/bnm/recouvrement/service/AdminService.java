package com.bnm.recouvrement.service;

import com.bnm.recouvrement.dao.PermissionRepository;
import com.bnm.recouvrement.dao.RoleRepository;
import com.bnm.recouvrement.dao.UserRepository;
import com.bnm.recouvrement.dto.AssignRoleRequest;
import com.bnm.recouvrement.dto.RoleRequest;
import com.bnm.recouvrement.dto.UpdateRequest;
import com.bnm.recouvrement.dto.UserDto;
import com.bnm.recouvrement.entity.Permission;
import com.bnm.recouvrement.entity.Role;
import com.bnm.recouvrement.entity.User;
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
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        // Use dynamic lookup rather than enum conversion
        Role role = roleRepository.findByName(request.getRole())
                .orElseThrow(() -> new RuntimeException("Role not found: " + request.getRole()));
        user.setRole(role);
        return userRepository.save(user);
    }
}
