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
public class AuthResponseDTO {

    // Le Token JWT généré par le JwtService
    private String token;
}