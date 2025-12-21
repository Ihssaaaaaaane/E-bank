package com.ebank.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Nous utilisons Lombok pour générer les Getters, Setters, et Constructeurs
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthRequestDTO {

    // Le login de l'utilisateur
    private String username;

    // Le mot de passe de l'utilisateur
    private String password;
}