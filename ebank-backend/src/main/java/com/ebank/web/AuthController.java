// src/main/java/com/ebank/web/AuthController.java
package com.ebank.web;

import com.ebank.dtos.AuthRequestDTO; // DTO pour Login/Password
import com.ebank.dtos.AuthResponseDTO; // DTO pour le JWT
import com.ebank.dtos.ChangePasswordRequest;
import com.ebank.dtos.ForgotPasswordRequest;
import com.ebank.dtos.ResetPasswordRequest;
import com.ebank.exceptions.InvalidCredentialsException;
import com.ebank.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.ebank.services.UserService; 
import com.ebank.services.PasswordResetService;

/**
 * Controller pour les opérations d'authentification
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    // UC-1: S'authentifier [cite: 30, 32]
    @PostMapping("/login")
    public AuthResponseDTO authenticate(@RequestBody AuthRequestDTO authRequest) {
        try {
            // Tente d'authentifier l'utilisateur
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            // Si l'authentification réussit (pas d'exception)
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails); // Génère le token

            // Retourne le token au client
            return new AuthResponseDTO(token);

        } catch (Exception e) {
            // RG_2: Si login ou mot de passe erronés 
            throw new RuntimeException("Login ou mot de passe erronés ");
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        try {
            String username = authentication.getName(); // Récupère le login de l'utilisateur connecté
            userService.changePassword(username, request);
            return ResponseEntity.ok("Le mot de passe a été mis à jour avec succès.");
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la mise à jour du mot de passe.");
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            passwordResetService.requestPasswordReset(request.getEmail());
            return ResponseEntity.ok("Si un compte existe pour cet email, un lien de réinitialisation a été envoyé.");
        } catch (Exception e) {
            return ResponseEntity.ok("Si un compte existe pour cet email, un lien de réinitialisation a été envoyé.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok("Mot de passe réinitialisé avec succès.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur lors de la réinitialisation du mot de passe.");
        }
    }
}