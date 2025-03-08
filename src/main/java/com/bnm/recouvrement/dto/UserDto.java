package com.bnm.recouvrement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private String name;
    private String email;
    private String password;
    private String role;
    private String userType; // Nouveau champ pour le type d'utilisateur
    private Long agenceId; // ID de l'agence associée
}
