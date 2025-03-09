package com.bnm.recouvrement.entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "app_user") // Naming the table explicitly
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    private String email;
    private String password;

    @Column(name = "user_type")
    private String userType;  // Nouveau champ pour stocker le type d'utilisateur
    
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
    
    @ManyToOne
    @JoinColumn(name = "agence_id")
    @JsonBackReference // Cette annotation empêche la sérialisation circulaire
    private Agence agence;

    // Methods from UserDetails interface

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        
        if (this.role == null) {
            // Default authority if role is null
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return authorities;
        }
        
        if ("ADMIN".equalsIgnoreCase(this.role.getName())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN")); // Correct role for Spring Security
        } else if ("AGENCE".equalsIgnoreCase(this.role.getName())) {
            authorities.add(new SimpleGrantedAuthority("ROLE_AGENCE"));
            if (this.role.getPermissions() != null) {
                this.role.getPermissions().forEach(permission -> 
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
                );
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + this.role.getName()));
            if (this.role.getPermissions() != null) {
                this.role.getPermissions().forEach(permission -> 
                    authorities.add(new SimpleGrantedAuthority(permission.getName()))
                );
            }
        }
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // Using email as the username for authentication
    }

    public String getName() {
        return name;
    }

    public void setUsername(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
    
    public Agence getAgence() {
        return agence;
    }

    public void setAgence(Agence agence) {
        this.agence = agence;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}